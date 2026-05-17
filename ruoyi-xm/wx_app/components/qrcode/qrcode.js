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
      const qr = generateQRCode(text)
      if (!qr) return

      const { matrix, size: qrSize } = qr
      const canvasSize = this.data.size
      const cellSize = canvasSize / (qrSize + 8) // 4 modules quiet zone on each side
      const offset = cellSize * 4

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

          // White background
          ctx.fillStyle = '#ffffff'
          ctx.fillRect(0, 0, canvasSize, canvasSize)

          // Draw modules
          ctx.fillStyle = '#000000'
          for (let r = 0; r < qrSize; r++) {
            for (let c = 0; c < qrSize; c++) {
              if (matrix[r][c] === 1) {
                const x = offset + c * cellSize
                const y = offset + r * cellSize
                ctx.fillRect(x, y, cellSize + 0.5, cellSize + 0.5)
              }
            }
          }
        })
    }
  }
})
