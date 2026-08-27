const fs = require("fs");
const path = require("path");

/**
 * Kotlin/Wasm webpack の context は kotlin-webpack 出力ディレクトリで、
 * Yarn が入れる build/wasm/node_modules を相対 "node_modules" では見つけられない。
 * Hosting の production バンドルで firebase / @js-joda/core が Module not found になる。
 *
 * 祖先の汎用 node_modules は見ない。Kotlin Yarn の build/wasm/node_modules だけを使う。
 */
function findDirWalkingUp(startDir, relativePath) {
    let current = startDir;
    for (let i = 0; i < 16; i++) {
        const candidate = path.join(current, relativePath);
        if (fs.existsSync(candidate)) {
            return candidate;
        }
        const parent = path.dirname(current);
        if (parent === current) {
            break;
        }
        current = parent;
    }
    return null;
}

config.resolve = config.resolve || {};
config.resolve.modules = config.resolve.modules || [];
const wasmNodeModules = findDirWalkingUp(__dirname, path.join("build", "wasm", "node_modules"));
if (wasmNodeModules && config.resolve.modules.indexOf(wasmNodeModules) === -1) {
    config.resolve.modules.unshift(wasmNodeModules);
}
