const axios = require('axios');

async function testApi() {
    try {
        const res = await axios.post('https://oyebintan-email-spam-classifier.hf.space/predict', {
            email: "Verify your account and claim $500 Amazon Gift Card"
        });
        console.log("Success:", res.data);
    } catch (e) {
        console.error("Error:", e.message, e.response?.data);
    }
}
testApi();
