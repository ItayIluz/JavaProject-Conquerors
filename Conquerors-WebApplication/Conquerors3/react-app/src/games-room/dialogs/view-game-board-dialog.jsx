import React, { Component } from 'react';
import GameBoard from '../../conquerors-game/game-board.jsx';
import './view-game-board-dialog.css';

class ViewGameBoardDialog extends Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Board Preview</div>
        <div className="container dialog-container view-game-board-content">
        {this.props.boardData != null ?
          <GameBoard
              boardRows={this.props.boardData.rows}
              boardColumns={this.props.boardData.columns}
              territories={this.props.boardData.territories}
              defaultTerritoryArmyThreshold={this.props.boardData.defaultTerritoryArmyThreshold}
              defaultTerritoryProfit={this.props.boardData.defaultTerritoryProfit}
          /> : null}
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default ViewGameBoardDialog; 