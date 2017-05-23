import React from 'react';
import ReactDOM from 'react-dom';
import Rnd from 'react-rnd';

window.deps = {
    'react' : React,
    'react-dom' : ReactDOM,
    'react-rnd': Rnd
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];
