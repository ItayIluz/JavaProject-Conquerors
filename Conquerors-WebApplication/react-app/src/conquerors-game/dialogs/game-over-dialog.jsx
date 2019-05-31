import React, { Component } from 'react';
import { Redirect } from "react-router-dom";
import './game-over-dialog.css';

class GameOverDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      startCountdown: false,
      leaveGameCounter: 10,
    }

    this.leaveGameCounterInterval = null;

    this.resetLeaveGameCounter = this.resetLeaveGameCounter.bind(this);
    this.leaveGameCountdown = this.leaveGameCountdown.bind(this);
  }

  resetLeaveGameCounter(){
    this.setState({startCountdown: true, leaveGameCounter: 10} , () => {
      this.leaveGameCounterInterval = setInterval(this.leaveGameCountdown, 1000);
    });
  }

  leaveGameCountdown(){
    if(this.state.startCountdown)
      this.setState({leaveGameCounter: this.state.leaveGameCounter - 1}, () => { 
        if(this.state.leaveGameCounter == 0) {
          clearInterval(this.leaveGameCounterInterval);
          this.props.leaveGameFunction();
        }
      });
  }

  componentWillUnmount(){
    clearInterval(this.leaveGameCounterInterval);
  }

  createTable() {
    let table = [];
    let gameResults = this.props.gameResults;
    if(gameResults){
      for (let i = 0; i < gameResults.length; i++) {
          let children = [];
    
          children.push(<td key={gameResults[i].player.id+"-"+i+"-id"}>{gameResults[i].player.id}</td>)
          children.push(<td key={gameResults[i].player.id+"-"+i+"-name"}>{gameResults[i].player.name}</td>)
          children.push(<td key={gameResults[i].player.id+"-"+i+"-num-of-territories"}>{gameResults[i].numOfTerritories}</td>)
          children.push(<td key={gameResults[i].player.id+"-"+i+"-total-territories-profit"}>{gameResults[i].totalTerritoriesProfit}</td>);

          table.push(<tr key={"player-"+gameResults[i].player.id} style={{background: gameResults[i].isWinner ? "green" : null}}>{children}</tr>)
      }
      return table;
    } else
      return null;
  }

  createWinnersMessage(){
    if(this.props.gameResults){

      let gameResults = this.props.gameResults;
      let message = "";
      let winners = [];

      for(let i = 0; i < gameResults.length; i++){
        if(gameResults[i].isWinner)
          winners.push(gameResults[i].player);
      }

      if(winners.length > 1) {
          message += "It's a tie! The winners are:\n";

          for(let i = 0; i < winners.length; i++)
              message += winners[i].name + "\n";

      } else {
        if(this.props.playerUsername == winners[0].name)
          message += "You are the winner!";
        else
          message += winners[0].name + " is the winner!";
      }

      return message.split("\n").map((i,key) => {
        return <div key={key}>{i}</div>;
      });
    } else 
      return null;
  }

  render() {      
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Game Over - Results</div>
        <div className="container dialog-container">
          <div className="game-over-details">
            {this.createWinnersMessage()}
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
          <div style={{margin: "5px 20px"}}><b>You will be redirected to the games room in <span>{this.state.leaveGameCounter}</span> seconds...</b></div>
        </div>
      </div>
    );
  }
}

export default GameOverDialog; 