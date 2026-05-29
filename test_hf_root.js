const https = require('https');

https.get('https://oyebintan-email-spam-classifier.hf.space/', (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log('STATUS:', res.statusCode, 'DATA:', data.substring(0, 300)));
});
