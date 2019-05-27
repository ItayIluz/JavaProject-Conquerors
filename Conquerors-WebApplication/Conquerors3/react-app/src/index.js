import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route } from "react-router-dom";
import "./index.css";

import SignUpPage from './sign-up-page/sign-up-page.jsx';
import GamesRoom from './games-room/games-room.jsx';
import ConquerorsGame from './conquerors-game/conquerors-game.jsx';

class App extends Component {

    constructor(props) {
        super(props);
    }

    render(){
        return (
            <div>
                <BrowserRouter>
                    <Route exact path="/Conquerors/" component={SignUpPage} />
                    <Route path="/Conquerors/games-room" component={GamesRoom} />
                    <Route path="/Conquerors/conquerors-game" component={ConquerorsGame} />
                </BrowserRouter>      
            </div>
        );
    }
}

ReactDOM.render(<App />, document.getElementById("root"));
