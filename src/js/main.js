import Draggable from 'react-drag';
import Resizable from 'react-resizable-box';
import DynamicFont from 'react-dynamic-font';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
    "dynamic-font": DynamicFont,
    "erd" : require("element-resize-detector")
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];


