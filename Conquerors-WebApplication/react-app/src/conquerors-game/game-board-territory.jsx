import React, { Component } from 'react';
import './game-board-territory.css';

class GameBoardTerritory extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div className="game-board-territory" style={{background: this.props.territoryData.isConquered ? this.props.territoryData.conqueringPlayer.color : "white"}}>
        <div className="property-id"><span>Territory #{this.props.territoryData.id}</span></div>
        <div className="property-profit"><span>Profit: {this.props.territoryData.profit}</span></div>
        <div className="property-threshold"><span>Threshold: {this.props.territoryData.armyThreshold}</span></div>
        <div className="property-conquered-by">
          <span>
            {this.props.territoryData.isConquered ? "Conquered By " + this.props.territoryData.conqueringPlayer.name : "Neutral"}
          </span>
        </div>
        <div className="action-buttons">
          {
            this.props.buttonMode && (this.props.takingActionOnTerritory || this.props.buttonMode == "View") ?
            <button 
              className="my-button territory-button"
              onClick={()=>this.props.openTerritoryArmiesDialog(this.props.territoryData, this.props.buttonMode)} 
            >
              {this.props.buttonMode}
            </button> : null
          }
        </div>
      </div>
    );
  }
}

GameBoardTerritory.defaultProps = {
  territoryData: {
    id: 0,
    profit: 0,
    armyThreshold: 0,
    isConquered: false,
    conqueringPlayer: null
  }
}

export default GameBoardTerritory; 