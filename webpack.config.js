const webpack = require('webpack');
const path = require('path');

const BUILD_DIR = path.resolve(__dirname, 'src', 'js');
const APP_DIR = path.resolve(__dirname, 'src', 'js');

const config = {
  entry: `${APP_DIR}/main.js`,
  output: {
    path: BUILD_DIR,
    filename: 'bundle.js'
  },

/*    module: {
	rules: [
	    {
		test: /\.js$/,
		exclude: /(node_modules|bower_components)/,
		use: {
		    loader: 'babel-loader'
		}
	    }
	]
    }*/
};

module.exports = config;
