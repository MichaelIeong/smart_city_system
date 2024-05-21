// runDeviceControl.js
const { setDeviceStatus } = require('./devicecontrol');

const args = process.argv.slice(2);
if (args.length < 1) {
    console.error("Please provide a status string as an argument.");
    process.exit(1);
}

const status = args[0];
const result = setDeviceStatus(status);
console.log(result);
