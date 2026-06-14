const app = require('./src/app');
const { PORT } = require('./src/config/env');

// Hanya jalankan listener jika tidak di-deploy sebagai Vercel Serverless Function
if (process.env.NODE_ENV !== 'production' || !process.env.VERCEL) {
  app.listen(PORT, () => {
    console.log(`🚀 Webservice berjalan di http://localhost:${PORT}`);
  });
}

// Export module untuk Vercel Serverless
module.exports = app;
