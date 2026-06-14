const app = require('./src/app');
const { PORT } = require('./src/config/env');

app.listen(PORT, () => {
  console.log(`Webservice berjalan di http://localhost:${PORT}`);
});
