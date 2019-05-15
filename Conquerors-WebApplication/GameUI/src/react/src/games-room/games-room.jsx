import React, { Component } from 'react';
import ActiveGamesTable from './active-games-table.jsx';
import ActivePlayersTable from './active-players-table.jsx';
import './games-room.css';

class GamesRoom extends Component {

  constructor(rops){
    super();
  }

  render() {
    return (
      <div className="main">
        <div className="main-header">
          Games Room
        </div>
        <div className="tables-container">
          <div className="container-header">
          <div className="container-header-title">Active Games</div>
            <div className="container">
              <ActiveGamesTable />
            </div>
          </div>
          
          <div className="container-header">
            <div className="container-header-title">Active Players</div>
            <div className="container">
              <ActivePlayersTable />
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default GamesRoom; 
