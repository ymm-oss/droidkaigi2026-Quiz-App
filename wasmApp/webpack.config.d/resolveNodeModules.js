const fs = require("fs");
const path = require("path");

/**
 * Kotlin/Wasm webpack の context は kotlin-webpack 出力ディレクトリで、
 * Yarn が入れる build/wasm/node_modules を相対 "node_modules" では見つけられない。
 * Hosting の production バンドルで firebase / @js-joda/core が Module not found になる。
 */
function collectNodeModuleDirs(startDir) {
    const found = [];
    let current = startDir;
    for (let i = 0; i < 16; i++) {
        const candidates = [
            path.join(current, "node_modules"),
            path.join(current, "build", "wasm", "node_modules"),
            path.join(current, "build", "js", "node_modules"),
        ];
        for (const candidate of candidates) {
            if (fs.existsSync(candidate) && !found.includes(candidate)) {
                found.push(candidate);
            }
        }
        const parent = path.dirname(current);
        if (parent === current) {
            break;
        }
        current = parent;
    }
    return found;
}

config.resolve = config.resolve || {};
config.resolve.modules = config.resolve.modules || [];
collectNodeModuleDirs(__dirname).forEach(function (dir) {
    if (config.resolve.modules.indexOf(dir) === -1) {
        config.resolve.modules.unshift(dir);
    }
});
