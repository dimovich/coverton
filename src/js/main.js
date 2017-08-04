import Draggable from 'react-draggable';
import Resizable from 'react-resizable-box';
import { SliderPicker } from 'react-color';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
    'semui': require ('semantic-ui-react'),
    'resize-detector': require("element-resize-detector"),
    'color-picker': SliderPicker
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];

