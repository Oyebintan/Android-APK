const https = require('https');

const req = https.request('https://oyebintan-email-spam-classifier.hf.space/predict', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
}, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log('STATUS:', res.statusCode, 'DATA:', data));
});
req.on('error', console.error);
req.write(JSON.stringify({ email: "Test urgent claim $500 gift card" }));
req.end();
