import Draggable from 'react-drag';
import Resizable from 'react-resizable-box';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];


