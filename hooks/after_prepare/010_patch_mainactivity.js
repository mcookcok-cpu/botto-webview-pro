#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const targetPath = path.resolve(__dirname, '../../platforms/android/app/src/main/java/com/botto/webview2/MainActivity.java');
const sourcePath = path.resolve(__dirname, '../../native/MainActivity.java');

if (fs.existsSync(sourcePath)) {
    const dir = path.dirname(targetPath);
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
    fs.copyFileSync(sourcePath, targetPath);
    console.log('Successfully patched MainActivity.java for Botto WebView Pro!');
} else {
    console.log('Source native MainActivity.java not found.');
}
