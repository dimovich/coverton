import Draggable from 'react-draggable';
import Resizable from 're-resizable';
import { SliderPicker } from 'react-color';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
   /* 'semui': require ('semantic-ui-react'),*/
    'react-color': SliderPicker
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];

