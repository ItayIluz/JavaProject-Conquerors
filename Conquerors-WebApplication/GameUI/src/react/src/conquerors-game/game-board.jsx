import React, { Component } from 'react';
import GameBoardTerritory from './game-board-territory.jsx';
 import './game-board.css';

class GameBoard extends Component {

  constructor(props){
    super(props);

    this.gameBoard = React.createRef();
    this.territoriesRefs = [];
    
    this.setGridColumnsStyle = this.setGridColumnsStyle.bind(this);
    this.showTerritoriesActionButton = this.showTerritoriesActionButton.bind(this);
  }

  createBoard() {    
    let board = [];
    let territories = this.props.territories.sort((a,b) => a.id - b.id);
    let i = 0;

    for(let row = 0; row < this.props.boardRows; row++){
      for (let column = 0; column < this.props.boardColumns; column++) {

        let territoryIndex = (row * this.props.boardColumns) + column;
        
        this.territoriesRefs.push(React.createRef());

        board.push(<GameBoardTerritory 
          key={"territory-#"+territoryIndex+1} 
          id={territoryIndex+1}
          profit={territories[i].id == territoryIndex ? territories[i].profit : this.props.territoryDefaultProfit}
          armyThreshold={territories[i].id == territoryIndex ? territories[i].armyThreshold : this.props.territoryDefaultArmyThreshold}
          ref={this.territoriesRefs[territoryIndex]}
          openTerritoryArmiesDialog={this.props.openTerritoryArmiesDialog}
          currentPlayer={this.props.currentPlayer}
        />);

        if(territories[i].id == territoryIndex)
          i++;
      }
    }
    return board;
  }

  // MAYBE LET SERVER DO ALL OF THIS????
  showTerritoriesActionButton(viewOnly) {
    let currentPlayer = this.props.currentPlayer;
    let isTerritoryInRangeArray = []; // calculate and retrieve from server

    for(let i = 0; i < this.territoriesRefs.length; i++) {
      let currentTerritory = this.territoriesRefs[i].current;
      
      if(viewOnly){

        if(currentPlayer != null && currentTerritory.props.conqueringPlayer != null &&  currentTerritory.props.conqueringPlayer.id == currentPlayer.id)
          currentTerritory.updateButtonMode("View");
        else
          currentTerritory.updateButtonMode("");

      } else {

        if(currentPlayer != null && currentTerritory.props.conqueringPlayer != null && currentTerritory.props.conqueringPlayer.id == currentPlayer.id)
          currentTerritory.updateButtonMode("Maintain");
        else if(isTerritoryInRangeArray[i] || currentPlayer.ownedTerritories.length == 0){

          if(currentTerritory.props.isConquered) 
            currentTerritory.updateButtonMode("Attack");
          else 
            currentTerritory.updateButtonMode("Conquer");
        
       } else 
          currentTerritory.updateButtonMode("");
      }
    }
  }

  componentDidMount(){
    this.setGridColumnsStyle();
    this.showTerritoriesActionButton(false);
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
            {this.createBoard()}
        </div>
    );
  }
}

export default GameBoard; 
