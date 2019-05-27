import React, { Component } from 'react';
import './game-over-dialog.css';

class GameOverDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      winningPlayers: []
    }
  }

  createTable() {
    let table = [];
    let winningPlayersData = this.state.winningPlayers;
    
    for (let i = 0; i < winningPlayersData.length; i++) {
        let children = []
  
        children.push(<td key={winningPlayersData[i].id+"-"+i+"-id"}>{winningPlayersData[i].id}</td>)
        children.push(<td key={winningPlayersData[i].id+"-"+i+"-name"}>{winningPlayersData[i].name}</td>)
        children.push(<td key={winningPlayersData[i].id+"-"+i+"-num-of-territories"}>{winningPlayersData[i].numOfTerritories}</td>)
        children.push(<td key={winningPlayersData[i].id+"-"+i+"-total-territories-profit"}>{winningPlayersData[i].totalTerritoriesProfit}</td>);

        table.push(<tr key={"player-"+winningPlayersData[i].id}>{children}</tr>)
    }
    return table;
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Territory Armies</div>
        <div className="container dialog-container">
          <div className="game-over-details">
            The Winner
          </div>
          <div>
            <table className="my-table dialog-table">
              <thead>
                <tr>
                  <th>Player ID</th>
                  <th>Player Name</th>
                  <th># Of Territories</th>
                  <th>Total Territories Profit</th>
                </tr>
              </thead>
              <tbody>
                {this.createTable()}
              </tbody>
            </table>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default GameOverDialog; 