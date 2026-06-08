param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$script:Results = New-Object System.Collections.Generic.List[object]

$App9901 = "wx_test_sp_mch_9901"
$App9902 = "wx_test_sp_mch_9902"
$App9903 = "wx_test_sp_mch_9903"
$App9904 = "wx_test_sp_mch_9904"

function Add-Result {
    param(
        [string]$Name,
        [bool]$Passed,
        [string]$Detail
    )
    $script:Results.Add([pscustomobject]@{
        Name = $Name
        Passed = $Passed
        Detail = $Detail
    }) | Out-Null
}

function Invoke-Service {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $uri = $BaseUrl.TrimEnd("/") + $Path
    $parameters = @{
        Method = $Method
        Uri = $uri
        Headers = $Headers
        ErrorAction = "Stop"
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $parameters.ContentType = "application/json; charset=utf-8"
        $parameters.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }

    try {
        $response = Invoke-WebRequest @parameters
        $status = [int]$response.StatusCode
        $content = [string]$response.Content
    } catch {
        if ($null -eq $_.Exception.Response) {
            throw
        }
        $status = [int]$_.Exception.Response.StatusCode
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $content = $reader.ReadToEnd()
        $reader.Dispose()
    }

    $json = $null
    if (-not [string]::IsNullOrWhiteSpace($content)) {
        try {
            $json = $content | ConvertFrom-Json
        } catch {
            $json = $null
        }
    }

    return [pscustomobject]@{
        HttpStatus = $status
        Json = $json
        Content = $content
        Uri = $uri
    }
}

function Is-AjaxSuccess {
    param([object]$Response)
    return $Response.HttpStatus -ge 200 -and $Response.HttpStatus -lt 300 -and
        $null -ne $Response.Json -and [int]$Response.Json.code -eq 200
}

function Assert-AjaxSuccess {
    param([string]$Name, [object]$Response)
    $msg = $Response.Content
    if ($null -ne $Response.Json -and $null -ne $Response.Json.msg) {
        $msg = [string]$Response.Json.msg
    }
    Add-Result $Name (Is-AjaxSuccess $Response) ("HTTP=" + $Response.HttpStatus + "; " + $msg)
}

function Assert-AjaxError {
    param([string]$Name, [object]$Response)
    $isError = $Response.HttpStatus -ge 400
    if ($null -ne $Response.Json -and $null -ne $Response.Json.code) {
        $isError = $isError -or ([int]$Response.Json.code -ne 200)
    }
    $msg = $Response.Content
    if ($null -ne $Response.Json -and $null -ne $Response.Json.msg) {
        $msg = [string]$Response.Json.msg
    }
    Add-Result $Name $isError ("HTTP=" + $Response.HttpStatus + "; " + $msg)
}

function Assert-True {
    param([string]$Name, [bool]$Condition, [string]$Detail)
    Add-Result $Name $Condition $Detail
}

function Get-ApiToken {
    param([string]$AppId)
    $login = Invoke-Service -Method "GET" -Path ("/wxmini/login/test?appid=" + $AppId)
    Assert-AjaxSuccess ("login test " + $AppId) $login
    if (-not (Is-AjaxSuccess $login)) {
        throw "Cannot continue because login failed for $AppId"
    }
    $token = [string]$login.Json.data.apiToken
    Assert-True ("token exists " + $AppId) (-not [string]::IsNullOrWhiteSpace($token)) "apiToken should be returned"
    return $token
}

function C-Headers {
    param([string]$AppId, [string]$Token = "")
    $headers = @{ "X-Wx-AppId" = $AppId }
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Wx-Authorization"] = "Bearer " + $Token
    }
    return $headers
}

function Merchant-Headers {
    param([string]$MerchantId, [string]$AppId, [string]$Token = "")
    $headers = @{
        "X-Merchant-Id" = $MerchantId
        "X-Merchant-AppId" = $AppId
    }
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Wx-Authorization"] = "Bearer " + $Token
    }
    return $headers
}

Write-Host "Running service-provider smoke tests against $BaseUrl"
Write-Host "Prerequisites: dev profile, wx.pay.stub-enabled=true, wxmini.login.test-enabled=true, full seed imported."

$token9901 = Get-ApiToken $App9901
$token9902 = Get-ApiToken $App9902
$token9903 = Get-ApiToken $App9903

$home9901 = Invoke-Service -Method "GET" -Path "/wxmini/merchant/home" -Headers (C-Headers $App9901)
Assert-AjaxSuccess "public merchant home 9901 by AppID" $home9901

$home9904 = Invoke-Service -Method "GET" -Path "/wxmini/merchant/home" -Headers (C-Headers $App9904)
Assert-AjaxError "stopped merchant 9904 is blocked" $home9904

$list9901 = Invoke-Service -Method "GET" -Path "/wxmini/groupon/list" -Headers (C-Headers $App9901)
Assert-AjaxSuccess "groupon list 9901" $list9901
$ids9901 = @($list9901.Json.data | ForEach-Object { [int64]$_.id })
Assert-True "groupon 9901 includes 993001" ($ids9901 -contains 993001) ("ids=" + ($ids9901 -join ","))
Assert-True "groupon 9901 excludes 993011" (-not ($ids9901 -contains 993011)) ("ids=" + ($ids9901 -join ","))

$list9902 = Invoke-Service -Method "GET" -Path "/wxmini/groupon/list" -Headers (C-Headers $App9902)
Assert-AjaxSuccess "groupon list 9902" $list9902
$ids9902 = @($list9902.Json.data | ForEach-Object { [int64]$_.id })
Assert-True "groupon 9902 includes 993011" ($ids9902 -contains 993011) ("ids=" + ($ids9902 -join ","))
Assert-True "groupon 9902 excludes 993001" (-not ($ids9902 -contains 993001)) ("ids=" + ($ids9902 -join ","))

$orders9901 = Invoke-Service -Method "GET" -Path "/wxmini/order/list" -Headers (C-Headers $App9901 $token9901)
Assert-AjaxSuccess "order list 9901" $orders9901

$replayWithWrongApp = Invoke-Service -Method "GET" -Path "/wxmini/order/list" -Headers (C-Headers $App9902 $token9901)
Assert-AjaxError "token 9901 cannot replay under AppID 9902" $replayWithWrongApp

$createOrder = Invoke-Service -Method "POST" -Path "/wxmini/order/create" -Headers (C-Headers $App9901 $token9901) -Body @{
    productId = 993001
    quantity = 1
    phone = "15099019901"
}
Assert-AjaxSuccess "create one-cent order 9901" $createOrder
$createdOrderNo = $null
if (Is-AjaxSuccess $createOrder) {
    $createdOrderNo = [string]$createOrder.Json.data.orderNo
    Assert-True "one-cent order amount returns 1 fen" ([int64]$createOrder.Json.data.payAmount -eq 1) ("payAmount=" + $createOrder.Json.data.payAmount)
}

if (-not [string]::IsNullOrWhiteSpace($createdOrderNo)) {
    $payCreate = Invoke-Service -Method "POST" -Path "/wxmini/pay/order/create" -Headers (C-Headers $App9901 $token9901) -Body @{
        orderNo = $createdOrderNo
    }
    Assert-AjaxSuccess "stub create partner pay order" $payCreate
    $packageValue = ""
    if ($null -ne $payCreate.Json -and $null -ne $payCreate.Json.data) {
        $packageValue = [string]$payCreate.Json.data.package
    }
    Assert-True "stub pay package uses prepay_id" ($packageValue.StartsWith("prepay_id=wx_stub_")) $packageValue
}

$mixedMerchantOrder = Invoke-Service -Method "POST" -Path "/wxmini/order/create" -Headers (C-Headers $App9901 $token9901) -Body @{
    productId = 993001
    quantity = 1
    items = @(
        @{ productId = 993001; quantity = 1 },
        @{ productId = 993011; quantity = 1 }
    )
}
Assert-AjaxError "mixed merchant order is blocked" $mixedMerchantOrder

$wrongTenantProduct = Invoke-Service -Method "POST" -Path "/wxmini/order/create" -Headers (C-Headers $App9901 $token9901) -Body @{
    productId = 993011
    quantity = 1
}
Assert-AjaxError "9901 token cannot buy 9902 product" $wrongTenantProduct

$missingConfigPay = Invoke-Service -Method "POST" -Path "/wxmini/pay/order/create" -Headers (C-Headers $App9903 $token9903) -Body @{
    orderNo = "TESTSP9903001"
}
Assert-AjaxError "merchant 9903 missing pay config is blocked" $missingConfigPay

$duplicateRefund = Invoke-Service -Method "POST" -Path "/wxmini/refund/apply" -Headers (C-Headers $App9901 $token9901) -Body @{
    orderNo = "TESTSP9901002"
    refundReason = "duplicate refund smoke"
}
Assert-AjaxError "duplicate refund request is blocked" $duplicateRefund

$merchantLogin = Invoke-Service -Method "POST" -Path "/wxmini/merchant-mini/auth/login" -Headers (Merchant-Headers "9901" $App9901) -Body @{
    username = "test_sp_mch9901_owner"
    password = "admin123"
    merchantId = 9901
}
Assert-AjaxSuccess "merchant mini login 9901 owner" $merchantLogin
$merchantToken = ""
if (Is-AjaxSuccess $merchantLogin) {
    $merchantToken = [string]$merchantLogin.Json.data.token
}
Assert-True "merchant mini token exists" (-not [string]::IsNullOrWhiteSpace($merchantToken)) "token should be returned"

if (-not [string]::IsNullOrWhiteSpace($merchantToken)) {
    $workbench = Invoke-Service -Method "GET" -Path "/wxmini/merchant-mini/workbench/overview" -Headers (Merchant-Headers "9901" $App9901 $merchantToken)
    Assert-AjaxSuccess "merchant mini workbench 9901" $workbench

    $wrongMerchantEntry = Invoke-Service -Method "GET" -Path "/wxmini/merchant-mini/workbench/overview" -Headers (Merchant-Headers "9902" $App9902 $merchantToken)
    Assert-AjaxError "merchant mini token cannot replay under merchant 9902" $wrongMerchantEntry
}

$script:Results | Format-Table -AutoSize

$failed = @($script:Results | Where-Object { -not $_.Passed })
if ($failed.Count -gt 0) {
    Write-Error ("Smoke failed: " + $failed.Count + " check(s) failed.")
    exit 1
}

Write-Host "Smoke passed: all checks succeeded."
