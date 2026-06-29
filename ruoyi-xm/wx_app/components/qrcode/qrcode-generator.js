/**
 * 轻量级 QR 码生成器
 * 支持 Version 1-6，Error Correction Level L
 * 适用于短文本（核销码、URL 等，48 字符以内）
 */

// --- Galois Field 工具 ---
const GF_EXP = new Array(512)
const GF_LOG = new Array(256)
;(function initGaloisField() {
  let x = 1
  for (let i = 0; i < 255; i++) {
    GF_EXP[i] = x
    GF_LOG[x] = i
    x <<= 1
    if (x >= 256) x ^= 0x11d
  }
  for (let i = 255; i < 512; i++) {
    GF_EXP[i] = GF_EXP[i - 255]
  }
})()

function gfMul(a, b) {
  if (a === 0 || b === 0) return 0
  return GF_EXP[GF_LOG[a] + GF_LOG[b]]
}

// --- Reed-Solomon ---
function rsGeneratorPoly(degree) {
  let poly = [1]
  for (let i = 0; i < degree; i++) {
    const newPoly = new Array(poly.length + 1).fill(0)
    for (let j = 0; j < poly.length; j++) {
      newPoly[j] ^= poly[j]
      newPoly[j + 1] ^= gfMul(poly[j], GF_EXP[i])
    }
    poly = newPoly
  }
  return poly
}

function rsEncode(data, ecLength) {
  const gen = rsGeneratorPoly(ecLength)
  const result = new Array(ecLength).fill(0)
  for (let i = 0; i < data.length; i++) {
    const feedback = data[i] ^ result[0]
    result.shift()
    result.push(0)
    if (feedback !== 0) {
      for (let j = 0; j < gen.length - 1; j++) {
        result[j] ^= gfMul(gen[j + 1], feedback)
      }
    }
  }
  return result
}

// --- QR 码参数表 ---
const EC_TABLE = {
  1: { totalCodewords: 26, ecCodewords: 7, group1Blocks: 1, group1Data: 19, group2Blocks: 0, group2Data: 0 },
  2: { totalCodewords: 44, ecCodewords: 10, group1Blocks: 1, group1Data: 34, group2Blocks: 0, group2Data: 0 },
  3: { totalCodewords: 70, ecCodewords: 15, group1Blocks: 1, group1Data: 55, group2Blocks: 0, group2Data: 0 },
  4: { totalCodewords: 100, ecCodewords: 20, group1Blocks: 1, group1Data: 80, group2Blocks: 0, group2Data: 0 },
  5: { totalCodewords: 134, ecCodewords: 26, group1Blocks: 1, group1Data: 108, group2Blocks: 0, group2Data: 0 },
  6: { totalCodewords: 172, ecCodewords: 18, group1Blocks: 2, group1Data: 68, group2Blocks: 0, group2Data: 0 }
}

const ALIGNMENT_PATTERNS = {
  2: [6, 18],
  3: [6, 22],
  4: [6, 26],
  5: [6, 30],
  6: [6, 34]
}

// --- 数据编码 ---
function encodeData(text, version) {
  const bits = []
  // Mode indicator: Alphanumeric = 0010
  bits.push(0, 0, 1, 0)

  // Character count
  const countBits = version <= 9 ? 9 : 11
  const len = text.length
  for (let i = countBits - 1; i >= 0; i--) {
    bits.push((len >> i) & 1)
  }

  // Alphanumeric encoding
  const ALPHA = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:'
  for (let i = 0; i < text.length; i += 2) {
    if (i + 1 < text.length) {
      const val = ALPHA.indexOf(text[i]) * 45 + ALPHA.indexOf(text[i + 1])
      for (let b = 9; b >= 0; b--) bits.push((val >> b) & 1)
    } else {
      const val = ALPHA.indexOf(text[i])
      for (let b = 5; b >= 0; b--) bits.push((val >> b) & 1)
    }
  }

  // Terminator
  const ec = EC_TABLE[version]
  const dataBits = ec.totalCodewords * 8 - ec.ecCodewords * (ec.group1Blocks + ec.group2Blocks) * 8 / ec.ecCodewords * ec.ecCodewords
  const capacity = ec.group1Data * ec.group1Blocks + (ec.group2Data || 0) * (ec.group2Blocks || 0)
  const capBits = capacity * 8

  const termLen = Math.min(4, capBits - bits.length)
  for (let i = 0; i < termLen; i++) bits.push(0)

  // Pad to byte boundary
  while (bits.length % 8 !== 0) bits.push(0)

  // Pad bytes
  const PAD_BYTES = [0xEC, 0x11]
  let padIdx = 0
  while (bits.length < capBits) {
    const pb = PAD_BYTES[padIdx % 2]
    for (let b = 7; b >= 0; b--) bits.push((pb >> b) & 1)
    padIdx++
  }

  // Convert to bytes
  const dataBytes = []
  for (let i = 0; i < bits.length; i += 8) {
    let byte = 0
    for (let j = 0; j < 8; j++) byte = (byte << 1) | (bits[i + j] || 0)
    dataBytes.push(byte)
  }

  return dataBytes.slice(0, capacity)
}

// --- 生成 EC 码字并交错 ---
function generateCodewords(dataBytes, version) {
  const ec = EC_TABLE[version]
  const allData = []
  const allEC = []

  // Group 1
  const g1DataLen = ec.group1Data
  for (let i = 0; i < ec.group1Blocks; i++) {
    const block = dataBytes.slice(i * g1DataLen, (i + 1) * g1DataLen)
    allData.push(block)
    allEC.push(rsEncode(block, ec.ecCodewords))
  }

  // Group 2
  if (ec.group2Blocks > 0) {
    const g2DataLen = ec.group2Data
    const offset = ec.group1Blocks * g1DataLen
    for (let i = 0; i < ec.group2Blocks; i++) {
      const block = dataBytes.slice(offset + i * g2DataLen, offset + (i + 1) * g2DataLen)
      allData.push(block)
      allEC.push(rsEncode(block, ec.ecCodewords))
    }
  }

  // Interleave data
  const result = []
  const maxDataLen = Math.max(...allData.map(b => b.length))
  for (let i = 0; i < maxDataLen; i++) {
    for (const block of allData) {
      if (i < block.length) result.push(block[i])
    }
  }

  // Interleave EC
  for (let i = 0; i < ec.ecCodewords; i++) {
    for (const block of allEC) {
      if (i < block.length) result.push(block[i])
    }
  }

  return result
}

// --- 构建矩阵 ---
function createMatrix(version) {
  const size = version * 4 + 17
  const matrix = []
  const reserved = []
  for (let i = 0; i < size; i++) {
    matrix.push(new Array(size).fill(0))
    reserved.push(new Array(size).fill(false))
  }
  return { matrix, reserved, size }
}

function placeFinder(m, row, col) {
  for (let r = -1; r <= 7; r++) {
    for (let c = -1; c <= 7; c++) {
      const rr = row + r
      const cc = col + c
      if (rr < 0 || rr >= m.size || cc < 0 || cc >= m.size) continue
      let val
      if (r === -1 || r === 7 || c === -1 || c === 7) {
        val = 0
      } else if (r === 0 || r === 6 || c === 0 || c === 6) {
        val = 1
      } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
        val = 1
      } else {
        val = 0
      }
      m.matrix[rr][cc] = val
      m.reserved[rr][cc] = true
    }
  }
}

function placeAlignment(m, row, col) {
  for (let r = -2; r <= 2; r++) {
    for (let c = -2; c <= 2; c++) {
      const rr = row + r
      const cc = col + c
      if (m.reserved[rr][cc]) return
    }
  }
  for (let r = -2; r <= 2; r++) {
    for (let c = -2; c <= 2; c++) {
      const rr = row + r
      const cc = col + c
      let val
      if (Math.abs(r) === 2 || Math.abs(c) === 2) {
        val = 1
      } else if (r === 0 && c === 0) {
        val = 1
      } else {
        val = 0
      }
      m.matrix[rr][cc] = val
      m.reserved[rr][cc] = true
    }
  }
}

function placeTimingPatterns(m) {
  for (let i = 8; i < m.size - 8; i++) {
    if (!m.reserved[6][i]) {
      m.matrix[6][i] = i % 2 === 0 ? 1 : 0
      m.reserved[6][i] = true
    }
    if (!m.reserved[i][6]) {
      m.matrix[i][6] = i % 2 === 0 ? 1 : 0
      m.reserved[i][6] = true
    }
  }
}

function reserveFormatArea(m) {
  // Around top-left finder
  for (let i = 0; i <= 8; i++) {
    if (!m.reserved[8][i]) m.reserved[8][i] = true
    if (!m.reserved[i][8]) m.reserved[i][8] = true
  }
  // Around top-right finder
  for (let i = m.size - 8; i < m.size; i++) {
    if (!m.reserved[8][i]) m.reserved[8][i] = true
  }
  // Around bottom-left finder
  for (let i = m.size - 7; i < m.size; i++) {
    if (!m.reserved[i][8]) m.reserved[i][8] = true
  }
  // Dark module
  m.matrix[m.size - 8][8] = 1
  m.reserved[m.size - 8][8] = true
}

function placeData(m, codewords) {
  let bitIdx = 0
  const bits = []
  for (const byte of codewords) {
    for (let b = 7; b >= 0; b--) bits.push((byte >> b) & 1)
  }

  let col = m.size - 1
  let upward = true

  while (col >= 0) {
    if (col === 6) col--
    const rowRange = upward
      ? Array.from({ length: m.size }, (_, i) => m.size - 1 - i)
      : Array.from({ length: m.size }, (_, i) => i)

    for (const row of rowRange) {
      for (let dc = 0; dc <= 1; dc++) {
        const c = col - dc
        if (c < 0) continue
        if (m.reserved[row][c]) continue
        m.matrix[row][c] = bitIdx < bits.length ? bits[bitIdx] : 0
        bitIdx++
      }
    }

    upward = !upward
    col -= 2
  }
}

// --- 掩码 ---
const MASK_FUNCTIONS = [
  (r, c) => (r + c) % 2 === 0,
  (r, c) => r % 2 === 0,
  (r, c) => c % 3 === 0,
  (r, c) => (r + c) % 3 === 0,
  (r, c) => (Math.floor(r / 2) + Math.floor(c / 3)) % 2 === 0,
  (r, c) => ((r * c) % 2 + (r * c) % 3) === 0,
  (r, c) => ((r * c) % 2 + (r * c) % 3) % 2 === 0,
  (r, c) => ((r + c) % 2 + (r * c) % 3) % 2 === 0
]

function applyMask(m, maskIdx) {
  const fn = MASK_FUNCTIONS[maskIdx]
  const result = m.matrix.map(row => [...row])
  for (let r = 0; r < m.size; r++) {
    for (let c = 0; c < m.size; c++) {
      if (!m.reserved[r][c] && fn(r, c)) {
        result[r][c] ^= 1
      }
    }
  }
  return result
}

function scoreMask(matrix, size) {
  let score = 0
  // Penalty 1: runs of same color
  for (let r = 0; r < size; r++) {
    let run = 1
    for (let c = 1; c < size; c++) {
      if (matrix[r][c] === matrix[r][c - 1]) {
        run++
        if (run === 5) score += 3
        else if (run > 5) score += 1
      } else {
        run = 1
      }
    }
  }
  for (let c = 0; c < size; c++) {
    let run = 1
    for (let r = 1; r < size; r++) {
      if (matrix[r][c] === matrix[r - 1][c]) {
        run++
        if (run === 5) score += 3
        else if (run > 5) score += 1
      } else {
        run = 1
      }
    }
  }
  return score
}

function placeFormatInfo(matrix, size, maskIdx) {
  // FORMAT_INFO is ordered by EC level M, L, H, Q; this generator uses L.
  const FORMAT_INFO = [
    0x5412, 0x5125, 0x5E7C, 0x5B4B, 0x45F9, 0x40CE, 0x4F97, 0x4AA0,
    0x77C4, 0x72F3, 0x7DAA, 0x789D, 0x662F, 0x6318, 0x6C41, 0x6976,
    0x1689, 0x13BE, 0x1CE7, 0x19D0, 0x0762, 0x0255, 0x0D0C, 0x083B,
    0x355F, 0x3068, 0x3F31, 0x3A06, 0x24B4, 0x2183, 0x2EDA, 0x2BED
  ]
  const info = FORMAT_INFO[8 + maskIdx]

  // Top-left: horizontal (row 8)
  const hPositions = [0, 1, 2, 3, 4, 5, 7, 8]
  for (let i = 0; i < 8; i++) {
    const bit = (info >> (14 - i)) & 1
    matrix[8][hPositions[i]] = bit
  }
  // Top-left: vertical (col 8)
  const vPositions = [8, 7, 5, 4, 3, 2, 1, 0]
  for (let i = 0; i < 8; i++) {
    const bit = (info >> (7 - i)) & 1
    matrix[vPositions[i]][8] = bit
  }

  // Bottom-left: vertical (col 8)
  for (let i = 0; i < 7; i++) {
    const bit = (info >> (14 - i)) & 1
    matrix[size - 1 - i][8] = bit
  }

  // Top-right: horizontal (row 8)
  for (let i = 0; i < 8; i++) {
    const bit = (info >> (7 - i)) & 1
    matrix[8][size - 8 + i] = bit
  }
}

// --- 主函数 ---
function generateQRCode(text) {
  const qrText = String(text || '').replace(/\s+/g, '').toUpperCase()
  if (!qrText) return null

  // Try alphanumeric mode
  const ALPHA = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:'
  const canAlpha = [...qrText].every(ch => ALPHA.includes(ch))
  if (!canAlpha) return null

  // Calculate required version
  let version = 1
  for (let v = 1; v <= 6; v++) {
    const ec = EC_TABLE[v]
    const capacity = ec.group1Data * ec.group1Blocks + (ec.group2Data || 0) * (ec.group2Blocks || 0)
    // Alphanumeric: 11 bits per 2 chars + 13 bits overhead
    const needed = canAlpha
      ? Math.ceil(13 + 9 + (Math.floor(qrText.length / 2) * 11 + (qrText.length % 2) * 6) / 8)
      : qrText.length + 3
    if (capacity >= needed) {
      version = v
      break
    }
    version = v + 1
  }

  if (version > 6) return null

  const size = version * 4 + 17

  // Encode data
  const dataBytes = encodeData(qrText, version)
  const codewords = generateCodewords(dataBytes, version)

  // Build matrix
  const m = createMatrix(version)
  placeFinder(m, 0, 0)
  placeFinder(m, 0, size - 7)
  placeFinder(m, size - 7, 0)

  if (ALIGNMENT_PATTERNS[version]) {
    const positions = ALIGNMENT_PATTERNS[version]
    for (const r of positions) {
      for (const c of positions) {
        if (r === 6 && c === 6) continue
        if (r === 6 && c === size - 7) continue
        if (r === size - 7 && c === 6) continue
        placeAlignment(m, r, c)
      }
    }
  }

  placeTimingPatterns(m)
  reserveFormatArea(m)
  placeData(m, codewords)

  // Find best mask
  let bestMask = 0
  let bestScore = Infinity
  let bestMatrix = null

  for (let i = 0; i < 8; i++) {
    const masked = applyMask(m, i)
    placeFormatInfo(masked, size, i)
    const score = scoreMask(masked, size)
    if (score < bestScore) {
      bestScore = score
      bestMask = i
      bestMatrix = masked
    }
  }

  return { matrix: bestMatrix, size }
}

module.exports = { generateQRCode }
