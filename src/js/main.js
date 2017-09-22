import Draggable from 'react-draggable';
import Resizable from 're-resizable';
import { SliderPicker } from 'react-color';
import { WithContext as ReactTags } from 'react-tag-input';

window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'draggable': Draggable,
    'resizable': Resizable,
    'react-color': SliderPicker,
    'react-tags': ReactTags
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];

