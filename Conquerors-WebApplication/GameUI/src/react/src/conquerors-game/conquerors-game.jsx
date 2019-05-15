import React, { Component } from 'react';
import GameBoard from './game-board.jsx';
import PlayersTable from './players-table.jsx';
 import './conquerors-game.css';

class ConquerorsGame extends Component {

  constructor(props){
    super(props);

    this.state = {
      totalRounds: 15,
      currentRound: 0,
      initialFunds: 3000,
      units: [
        {
          rank: 1,
          type: "Soldier",
          purchase: 80,
          maxFirePower: 100,
          competenceReduction: 20
        },
        {
          rank: 2,
          type: "Tank",
          purchase: 150,
          maxFirePower: 350,
          competenceReduction: 30
        },
        {
          rank: 3,
          type: "Missile",
          purchase: 300,
          maxFirePower: 600,
          competenceReduction: 70
        },
      ],
      boardRows: 8,
      boardColumns: 8,
      territoryDefaultProfit: 0,
      territoryDefaultArmyThreshold: 10,
      territories: [
        {
          id: 1,
          profit: 100,
          armyThreshold: 200,
        },
        {
          id: 13,
          profit: 300,
          armyThreshold: 150,
        },
        {
          id: 15,
          profit: 250,
          armyThreshold: 120,
        },
        {
          id: 11,
          profit: 2200,
          armyThreshold: 1100,
        },
        {
          id: 63,
          profit: 10,
          armyThreshold: 100,
        },
        {
          id: 25,
          profit: 100,
          armyThreshold: 10,
        },
        {
          id: 36,
          profit: 10,
          armyThreshold: 100,
        },
        {
          id: 48,
          profit: 20,
          armyThreshold: 20,
        },
        {
          id: 49,
          profit: 2000,
          armyThreshold: 1000,
        },
        {
          id: 50,
          profit: 200,
          armyThreshold: 150,
        },
      ],
      players: [
        {
          id: 233,
          name: "Mushon",
          ownedTerritories: [],
        },
        {
          id: 143,
          name: "Shoshke",
          ownedTerritories: [],
        },
        { 
          id: 456,
          name: "Nachtze",
          ownedTerritories: [],
        },
        {
          id: 893,
          name: "Tikva",
          ownedTerritories: [],
        }
      ],
      currentPlayerIndex: 1,
      territoriesRefs: [],
    }

    let colors = [
      { name: "Red", color: "tomato" },
      { name: "Blue", color: "lightblue" },
      { name: "Yellow", color: "gold" },
      { name: "Green", color: "lightgreen" },
    ];

    for(let i = 0; i < this.state.players.length; i++){
      this.state.players[i].color = colors[i].color;
      this.state.players[i].colorName = colors[i].name;
      this.state.players[i].money = this.state.initialFunds;
    }

    this.gameBoard = React.createRef();
  }

  componentDidMount(){
    
  }

  render() {
    return (
      <div className="main">
        <div className="main-header">
          Game - {this.props.gameName}
        </div>
        <div className="main-game-container">
          <div className="container-header">
            <div className="container-header-title">Active Games</div>
            <div className="container conquerors-container">
              <div className="game-details">
                <span><b>Current Round:</b> {this.state.currentRound} / {this.state.totalRounds}</span>
                {this.state.currentPlayer ? <span><b>Current Player:</b> {this.state.currentPlayer}</span> : null}
              </div>
              <div className="game-controls">
                <button className="my-button" disabled>Take Action On Territory</button>
                <button className="my-button" disabled>End Turn</button>
                <button className="my-button" disabled>Surrender</button>
              </div>
              <PlayersTable 
                playersData={this.state.players}
                initialFunds={this.state.initialFunds}
              />
              <GameBoard
                ref={this.gameBoard}
                boardRows={this.state.boardRows}
                boardColumns={this.state.boardColumns}
                territories={this.state.territories}
                territoryDefaultProfit={this.state.territoryDefaultProfit}
                territoryDefaultArmyThreshold={this.state.territoryDefaultArmyThreshold}
                territoriesRefs={this.state.territoriesRefs}
                currentPlayer={this.state.players[this.state.currentPlayerIndex]}
                openTerritoryArmiesDialog={this.props.openTerritoryArmiesDialog}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default ConquerorsGame; 
