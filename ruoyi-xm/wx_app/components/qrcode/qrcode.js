const { generateQRCode } = require('./qrcode-generator')

Component({
  properties: {
    text: {
      type: String,
      value: '',
      observer(val) {
        if (val) this.drawQRCode(val)
      }
    },
    size: {
      type: Number,
      value: 360
    }
  },

  data: {
    canvasId: 'qr-canvas'
  },

  lifetimes: {
    ready() {
      if (this.data.text) {
        this.drawQRCode(this.data.text)
      }
    }
  },

  methods: {
    drawQRCode(text) {
      const qrText = String(text || '').replace(/\s+/g, '').toUpperCase()
      const qr = generateQRCode(qrText)
      if (!qr) return

      const { matrix, size: qrSize } = qr
      const canvasSize = this.data.size
      const quietZone = 4
      const totalModules = qrSize + quietZone * 2
      const cellSize = Math.max(1, Math.floor(canvasSize / totalModules))
      const qrPixelSize = cellSize * totalModules
      const offset = Math.floor((canvasSize - qrPixelSize) / 2) + cellSize * quietZone

      const query = wx.createSelectorQuery().in(this)
      query.select('#qr-canvas')
        .fields({ node: true, size: true })
        .exec((res) => {
          if (!res || !res[0] || !res[0].node) return
          const canvas = res[0].node
          const ctx = canvas.getContext('2d')

          const dpr = wx.getWindowInfo().pixelRatio || 2
          canvas.width = canvasSize * dpr
          canvas.height = canvasSize * dpr
          ctx.scale(dpr, dpr)
          ctx.imageSmoothingEnabled = false

          ctx.fillStyle = '#ffffff'
          ctx.fillRect(0, 0, canvasSize, canvasSize)

          ctx.fillStyle = '#000000'
          for (let r = 0; r < qrSize; r++) {
            for (let c = 0; c < qrSize; c++) {
              if (matrix[r][c] === 1) {
                const x = offset + c * cellSize
                const y = offset + r * cellSize
                ctx.fillRect(x, y, cellSize, cellSize)
              }
            }
          }
        })
    }
  }
})
