const puppeteer = require('puppeteer');

const args = process.argv.slice(0);

const URL_PARAM = "--url=";
const OUTFILE_PARAM = "--outfile=";


let url = null;
let outfile = null;

for(let i = 0; i < args.length; i++) {
    let arg = args[i];
    if(arg.startsWith(URL_PARAM)) {
        url = arg.substring(URL_PARAM.length);
    }
    else if(arg.startsWith(OUTFILE_PARAM)) {
        outfile = arg.substring(OUTFILE_PARAM.length);
    }
}

if(url == null || outfile == null) {
    throw new Error("The url and outfile parameters were not found.");
}


async function canTellReportReady(page) {
    return await page.waitForFunction('window.canTellReportReady == true', {
        polling: 200,
        timeout: 5000
    }).then(function(result) { return true; }, function() { return false; });
}

async function isReportReady(page) {
    return await page.waitForFunction('window.reportReady == true', {
        polling: 200,
        timeout: 90000
    }).then(function(result) { return true; }, function() { return false; });
}


(async () => {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    await page.goto(url, {waitUntil: 'networkidle2'});
    let ctrr = await canTellReportReady(page);
    if(ctrr) {
        await isReportReady(page);
    }
    await page.pdf({path: outfile, format: 'A4'});

    await browser.close();
})();
