import Draggable from 'react-draggable';
import Resizable from 'react-resizable-box';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
    'semui': require ('semantic-ui-react'),
    'resize-detector': require("element-resize-detector")
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];

