import React, { Component } from 'react';
import './game-board-territory.css';

class GameBoardTerritory extends Component {

  constructor(props) {
    super(props);

    this.state = {
      buttonMode: "" // View/Maintain/Conquer/Attack
    }

    this.preOpenDialog = this.preOpenDialog.bind(this);
    this.updateButtonMode = this.updateButtonMode.bind(this);
  }

  updateButtonMode(newMode) { 
    this.setState({buttonMode: newMode});
  }

  preOpenDialog(){
    this.props.openTerritoryArmiesDialog({
      id: this.props.id,
      profit: this.props.profit,
      armyThreshold: this.props.armyThreshold,
      isConquered: this.props.isConquered,
      conqueringPlayer: this.props.conqueringPlayer
    }, this.props.currentPlayer, this.state.buttonMode);
  }

  render() {
    return (
      <div className="game-board-territory">
        <div className="property-id"><span>Territory #{this.props.id}</span></div>
        <div className="property-profit"><span>Profit: {this.props.profit}</span></div>
        <div className="property-threshold"><span>Threshold: {this.props.armyThreshold}</span></div>
        <div className="property-conquered-by">
          <span>
            {this.props.isConquered ? "Conquered By " + this.props.conqueringPlayer : "Neutral"}
          </span>
        </div>
        <div className="action-buttons">
          <button 
            className="my-button territory-button"
            onClick={this.preOpenDialog} 
            style={{display: (this.state.buttonMode ? "inline" : "none")}}>
            {this.state.buttonMode}
          </button>
        </div>
      </div>
    );
  }
}

GameBoardTerritory.defaultProps = {
  isConquered: false,
  conqueringPlayer: null
}

export default GameBoardTerritory; 