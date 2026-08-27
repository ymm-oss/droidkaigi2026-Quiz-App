const path = require("path");

// This file is inlined into build/wasm/packages/<app>/webpack.config.js.
// Gradle の cwd では相対 "node_modules" が Yarn の hoist 先を見ない。
config.resolve.modules.unshift(path.resolve(__dirname, "../../node_modules"));
