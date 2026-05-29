const https = require('https');

https.get('https://huggingface.co/spaces/Oyebintan/email-spam-classifier/raw/main/app.py', (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log('DATA:\n', data));
});
