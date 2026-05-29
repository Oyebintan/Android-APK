const http = require('https');

http.get('https://oyebintan-email-spam-classifier.hf.space/predict', (res) => {
  console.log('Status:', res.statusCode);
  res.on('data', d => process.stdout.write(d));
}).on('error', e => console.error(e));
