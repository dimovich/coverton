import Draggable from 'react-draggable';
import Resizable from 'react-resizable-box';
import { Button } from 'semantic-ui-react';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
    'semui-button': Button
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];

