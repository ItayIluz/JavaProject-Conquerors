import React, { Component } from 'react';
import GameBoard from './game-board.jsx';
import PlayersTable from './players-table.jsx';
import { Redirect } from "react-router-dom";
import {postActionTo, getDataFrom} from '../server-functions.js';
import Modal from 'react-awesome-modal';
import TerritoryArmiesDialog from "./dialogs/territory-armies-dialog.jsx";
import AddArmyDialog from './dialogs/add-army-dialog.jsx';
import AttackResultsDialog from './dialogs/attack-results-dialog.jsx';
import GameOverDialog from './dialogs/game-over-dialog.jsx';
import WaitingForPlayersDialog from './dialogs/waiting-for-players-dialog.jsx';
import './conquerors-game.css';

const SERVLET_URL = "conquerorsGame";

class ConquerorsGame extends Component {

  constructor(props){
    super(props);

    this.state = {
      gameTitle: this.props.location.state.gameTitle,
      gameData: null,
      leftGame: false,
      territoriesRefs: [],
      territoryArmiesDialog: {
          visible: false,
          territory: null,
          currentPlayer: null,
          mode: "Conquer"
      },
      addArmyDialog: {
          visible: false,
          currentPlayer: null,
          territory: null,
      },
      attackResultsDialog: {
          visible: false,
          attackType: "",
          territory: null,
          attackingArmy: null,
          attackingPlayer: null,
          defendingArmy: null,
          defendingPlayer: null
      },
      gameOverDialog: {
          visible: false
      },
      waitingForPlayersDialog: {
        visible: false,
      },
    }

    this.gameBoard = React.createRef();

    this.refreshDataInterval = null;
    this.refreshData = this.refreshData.bind(this);
    this.openTerritoryArmiesDialog = this.openTerritoryArmiesDialog.bind(this);
    this.closeTerritoryArmiesDialog = this.closeTerritoryArmiesDialog.bind(this);
    this.openAddArmyDialog = this.openAddArmyDialog.bind(this);
    this.closeAddArmyDialog = this.closeAddArmyDialog.bind(this);
    this.openAttackResultsDialog = this.openAttackResultsDialog.bind(this);
    this.closeAttackResultsDialog = this.closeAttackResultsDialog.bind(this);
    this.openGameOverDialog = this.openGameOverDialog.bind(this);
    this.closeGameOverDialog = this.closeGameOverDialog.bind(this);
    this.leaveGameFunction = this.leaveGameFunction.bind(this);
  }

  componentDidMount() {
    this.refreshData();
    this.refreshDataInterval = setInterval(this.refreshData, 2000);
  }

  componentWillUnmount(){
    clearInterval(this.refreshDataInterval);
  }

  refreshData(){
    getDataFrom(SERVLET_URL, responseJSON => {
      this.setState({
        gameData: responseJSON.gameData
      });
    }, {gameTitle: this.state.gameTitle});
  }

  openTerritoryArmiesDialog(territoryData, currentPlayerData, modeData) {
      this.setState({
          territoryArmiesDialog: {
              visible: true,
              territory: territoryData,
              currentPlayer: currentPlayerData,
              mode: modeData
          }
      });
  }

  closeTerritoryArmiesDialog() {
      this.setState({
          territoryArmiesDialog: {
              visible: false,
              territory: null,
          }
      });
  }

  openAddArmyDialog(territoryData, currentPlayerData) {
      this.setState({
          addArmyDialog: {
              visible: true,
              territory: territoryData,
              currentPlayer: currentPlayerData,
          }
      });
  }

  closeAddArmyDialog() {
      this.setState({
          addArmyDialog: {
              visible: false,
              territory: null,
          }
      });
  }

  openAttackResultsDialog(territoryData, attackingArmyData, defendingArmyData, attackingPlayerData, defendingPlayerData, attackTypeData) {
      this.setState({
          attackResultsDialog: {
              visible: true,
              attackType: attackTypeData,
              territory: territoryData,
              attackingArmy: attackingArmyData,
              attackingPlayer: attackingPlayerData,
              defendingArmy: defendingArmyData,
              defendingPlayer: defendingPlayerData
          }
      });
  }

  closeAttackResultsDialog() {
      this.setState({
          attackResultsDialog: {
              visible: false,
              attackType: "",
              territory: null,
              attackingArmy: null,
              attackingPlayer: null,
              defendingArmy: null,
              defendingPlayer: null
          }
      });
  }

  openGameOverDialog() {
      this.setState({
          gameOverDialog: {
              visible: true,
          }
      });
  }

  closeGameOverDialog() {
      this.setState({
          gameOverDialog: {
              visible: false,
          }
      });
  }

  leaveGameFunction() {
    postActionTo(SERVLET_URL, "leaveGame", {gameTitle: this.state.gameTitle}, () => this.setState({leftGame: true}));
  }

  render() {
    if(this.state.leftGame) 
      return <Redirect to="/Conquerors/games-room" />;

    return (
      <div className="main">
        <div className="main-header">
          <span>Game - {this.state.gameTitle} {this.state.gameData != null ? <i>({this.state.gameData.gameStatus})</i> : null}</span>
        </div>
        <div className="main-game-container">
          <div className="container-header">
            <div className="container-header-title">Active Games</div>
            <div className="container conquerors-container">
              <div className="game-details">
                {this.state.gameData != null ? <span><b>Current Round:</b> {this.state.gameData.currentRound} / {this.state.gameData.totalRounds}</span> : null}
                {
                  this.state.gameData != null && this.state.gameData.currentPlayerIndex > -1 ? 
                  <span><b>Current Player:</b> {this.state.gameBoard.players[this.state.gameBoard.currentPlayerIndex].name}</span> 
                  : null
                }
              </div>
              <div className="game-controls">
                <button className="my-button" disabled>Take Action On Territory</button>
                <button className="my-button" disabled>End Turn</button>
                <button className="my-button" disabled>Surrender</button>
              </div>
              {
                this.state.gameData != null ?
                <PlayersTable 
                  playersData={this.state.gameData.players}
                  initialFunds={this.state.gameData.initialFunds}
                /> : null
              }
              {
                this.state.gameData != null ?
                <GameBoard
                  ref={this.gameBoard}
                  boardRows={this.state.gameData.gameBoard.rows}
                  boardColumns={this.state.gameData.gameBoard.columns}
                  territories={this.state.gameData.gameBoard.territories}
                  defaultTerritoryArmyThreshold={this.state.gameData.gameBoard.defaultTerritoryArmyThreshold}
                  defaultTerritoryProfit={this.state.gameData.gameBoard.defaultTerritoryProfit}
                  territoriesRefs={this.state.territoriesRefs}
                  currentPlayer={this.state.gameData.players[this.state.gameData.currentPlayerIndex]}
                  openTerritoryArmiesDialog={this.props.openTerritoryArmiesDialog}
                /> : null
              }
              <Modal visible={this.state.territoryArmiesDialog.visible} effect="fadeInUp">
                  <TerritoryArmiesDialog 
                      territory={this.state.territoryArmiesDialog.territory} 
                      currentPlayer={this.state.territoryArmiesDialog.currentPlayer} 
                      mode={this.state.territoryArmiesDialog.mode}
                      openAddArmyDialogFunc={this.openAddArmyDialog}
                      closeFunction={this.closeTerritoryArmiesDialog}
                  />
              </Modal>
              <Modal visible={this.state.addArmyDialog.visible} effect="fadeInUp">
                  <AddArmyDialog 
                      territory={this.state.addArmyDialog.territory} 
                      currentPlayer={this.state.addArmyDialog.currentPlayer} 
                      mode={this.state.addArmyDialog.mode}
                      closeFunction={this.closeAddArmyDialog}
                  />
              </Modal>
              <Modal visible={this.state.attackResultsDialog.visible} effect="fadeInUp">
                  <AttackResultsDialog 
                      attackType={this.state.attackResultsDialog.attackType}
                      territory={this.state.attackResultsDialog.territory}
                      attackingArmy={this.state.attackResultsDialog.attackingArmy}
                      attackingPlayer={this.state.attackResultsDialog.attackingPlayer}
                      defendingArmy={this.state.attackResultsDialog.defendingArmy}
                      defendingPlayer={this.state.attackResultsDialog.defendingPlayer}
                      closeFunction={this.closeAttackResultsDialog}
                  />
              </Modal>
              <Modal visible={this.state.gameOverDialog.visible} effect="fadeInUp">
                  <GameOverDialog 
                      closeFunction={this.closeGameOverDialog}
                  />
              </Modal>
              {
                this.state.gameData ?
                <Modal visible={this.state.gameData.totalPlayers != this.state.gameData.currentPlayers} effect="fadeInUp">
                    <WaitingForPlayersDialog 
                        leaveGameFunction={this.leaveGameFunction}
                        allPlayersAreIn={this.state.gameData.totalPlayers == this.state.gameData.currentPlayers}
                    />
                </Modal> : null
              }
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default ConquerorsGame; 
