const qrcode = require('./qrcode-vendor')

function generateQRCode(text) {
  const qrText = String(text || '').replace(/\s+/g, '').toUpperCase()
  if (!qrText) return null

  try {
    const qr = qrcode(0, 'M')
    qr.addData(qrText)
    qr.make()

    const size = qr.getModuleCount()
    const matrix = []
    for (let row = 0; row < size; row += 1) {
      const line = []
      for (let col = 0; col < size; col += 1) {
        line.push(qr.isDark(row, col) ? 1 : 0)
      }
      matrix.push(line)
    }
    return { matrix, size }
  } catch (e) {
    return null
  }
}

module.exports = { generateQRCode }
