import React, { Component } from 'react';
import GameBoardTerritory from './game-board-territory.jsx';
 import './game-board.css';

class GameBoard extends Component {

  constructor(props){
    super(props);

    this.gameBoard = React.createRef();
    this.territoriesRefs = [];
    
    this.setGridColumnsStyle = this.setGridColumnsStyle.bind(this);
  }

  createBoard() {    
    let board = [];
    let territories = this.props.territories;

    for(let i = 0; i < territories.length; i++){
        
        this.territoriesRefs.push(React.createRef());

        let territoryButtonMode = this.props.actionsOnTerritories.length != 0 ? this.props.actionsOnTerritories[i] : null;
        if(territoryButtonMode == null && territories[i].isConquered){
          if(this.props.playerUsername == territories[i].conqueringPlayer.name)
            territoryButtonMode = "View";
        }

        board.push(<GameBoardTerritory 
          key={"territory-#"+territories[i].id}
          territoryData={territories[i]}
          ref={this.territoriesRefs[i]}
          openTerritoryArmiesDialog={this.props.openTerritoryArmiesDialog}
          buttonMode={territoryButtonMode}
          takingActionOnTerritory={this.props.takingActionOnTerritory}
        />);
    }

    return board;
  }

  componentDidMount(){
    this.setGridColumnsStyle();
  }

  setGridColumnsStyle(){
    let totalGridColumns = "";
    for(let i = 0; i < this.props.boardColumns; i++)
        totalGridColumns += " 1fr";

    this.gameBoard.current.style["grid-template-columns"] = totalGridColumns;
  }

  render() {
    return (
        <div className="game-board" ref={this.gameBoard}>
            {this.props.territories ? this.createBoard() : null}
        </div>
    );
  }
}

GameBoard.defaultProps = {
  takingActionOnTerritory: false,
  actionsOnTerritories: []
}

export default GameBoard; 
