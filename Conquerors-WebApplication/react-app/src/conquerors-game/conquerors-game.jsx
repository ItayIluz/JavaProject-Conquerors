import React, { Component } from 'react';
import GameBoard from './game-board.jsx';
import PlayersTable from './players-table.jsx';
import { Redirect } from "react-router-dom";
import {postActionTo, getDataFrom} from '../server-functions.js';
import Modal from 'react-awesome-modal';
import TerritoryArmiesDialog from "./dialogs/territory-armies-dialog.jsx";
import GameOverDialog from './dialogs/game-over-dialog.jsx';
import WaitingForPlayersDialog from './dialogs/waiting-for-players-dialog.jsx';
import './conquerors-game.css';
import PlayerTurnNotificationDialog from './dialogs/player-turn-notification-dialog.jsx';
import LastPlayerActionDialog from './dialogs/last-player-action-dialog.jsx';

const SERVLET_URL = "conquerorsGame";

class ConquerorsGame extends Component {

  constructor(props){
    super(props);

    this.state = {
      gameTitle: this.props.location.state.gameTitle,
      playerUsername: this.props.location.state.playerUsername,
      gameData: null,
      leftGame: false,
      currentPlayer: null,
      isUsersTurn: false,
      takingActionOnTerritory: false,
      actionsOnTerritories: [],
      territoriesRefs: [],
      canTakeAction: true,
      isGameOver: false,
      winningPlayers: null,
      territoryArmiesDialog: {
          visible: false,
      },
      gameOverDialog: {
          visible: false,
      },
      waitingForPlayersDialog: {
        visible: false,
      },
      playerTurnNotificationDialog: {
        visible: false,
        confirmed: false,
      },
      lastPlayerActionDialog: {
        visible: false,
        confirmed: false,
      },
      lastPlayerAction: null,
      lastPlayerName: null,
    }
    
    this.gameOverDialogRef = React.createRef();
    this.gameBoard = React.createRef();
    this.territoryArmiesDialogRef = React.createRef();

    this.refreshDataInterval = null;
    this.refreshData = this.refreshData.bind(this);
    this.openTerritoryArmiesDialog = this.openTerritoryArmiesDialog.bind(this);
    this.closeTerritoryArmiesDialog = this.closeTerritoryArmiesDialog.bind(this);
    this.openGameOverDialog = this.openGameOverDialog.bind(this);
    this.handleLeaveGame = this.handleLeaveGame.bind(this);
    this.handleTakeAction = this.handleTakeAction.bind(this);
    this.handleEndTurn = this.handleEndTurn.bind(this);
    this.handleSurrender = this.handleSurrender.bind(this);
    this.handleGameOver = this.handleGameOver.bind(this);
    this.closePlayerTurnNotificationDialog = this.closePlayerTurnNotificationDialog.bind(this);
    this.closelastPlayerActionDialog = this.closelastPlayerActionDialog.bind(this);
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
        gameData: responseJSON.gameData,
        currentPlayer: responseJSON.gameData.players[responseJSON.gameData.currentPlayerIndex],
        isUsersTurn: responseJSON.isUsersTurn,
        canTakeAction: responseJSON.gameData.canCurrentPlayerTakeAction,
        lastPlayerName: responseJSON.lastPlayerName ? responseJSON.lastPlayerName : null,
        lastPlayerAction: responseJSON.lastPlayerAction ? responseJSON.lastPlayerAction : null,
        isGameOver: responseJSON.gameData.gameOver,
        gameResults: responseJSON.gameData.gameOver ? responseJSON.gameResults : null
      }, () => { 

        if(this.state.isGameOver){
          this.setState({ lastPlayerActionDialog: {visible: true, confirmed: this.state.lastPlayerActionDialog.confirmed} });
          this.handleGameOver(); 
        }else if(this.state.isUsersTurn)
          this.setState({ 
            playerTurnNotificationDialog: {visible: true, confirmed: this.state.playerTurnNotificationDialog.confirmed},
            lastPlayerActionDialog: {visible: true, confirmed: this.state.lastPlayerActionDialog.confirmed}
          });
      });
    }, {get: "gameData", gameTitle: this.state.gameTitle});
  }

  handleGameOver(){
    this.openGameOverDialog();
    clearInterval(this.refreshDataInterval);
  }

  handleTakeAction(){
    getDataFrom(SERVLET_URL, responseJSON => {
      this.setState({
        takingActionOnTerritory: true,
        actionsOnTerritories: responseJSON.actionsOnTerritories
      });
    }, {get: "actionsOnTerritories", gameTitle: this.state.gameTitle})
  }

  handleEndTurn(){
    postActionTo(SERVLET_URL, "endTurn", {gameTitle: this.state.gameTitle}, () => {
      this.setState({
        isUsersTurn: false,
        takingActionOnTerritory: false,
        actionsOnTerritories: [],
        playerTurnNotificationDialog: {visible: false, confirmed: false},
        lastPlayerActionDialog: {visible: false, confirmed: false},
      });
    });
  }

  handleSurrender(){
    postActionTo(SERVLET_URL, "playerSurrender", {gameTitle: this.state.gameTitle}, () => this.setState({leftGame: true}));
  }

  handleLeaveGame() {
    postActionTo(SERVLET_URL, "leaveGame", {gameTitle: this.state.gameTitle}, () => this.setState({leftGame: true}));
  }

  openTerritoryArmiesDialog(territoryData, modeData) {
      this.territoryArmiesDialogRef.current.onOpenDialog(territoryData, modeData);
      this.setState(() => {
        return {
          territoryArmiesDialog: {visible: true,}
        }
      });
  }

  closeTerritoryArmiesDialog(tookAction) {

    if(tookAction) {
      this.setState({
        takingActionOnTerritory: false,
        canTakeAction: false,
        actionsOnTerritories: [],
        territoryArmiesDialog: {
          visible: false
        }
      }, () => this.refreshData());
    } else {
      this.setState({
        actionsOnTerritories: [],
        territoryArmiesDialog: {
          visible: false
        }
      });
    }
  }

  openGameOverDialog() {
    this.setState({gameOverDialog: {visible: true}}, () => this.gameOverDialogRef.current.resetLeaveGameCounter());
  }

  closePlayerTurnNotificationDialog() {
    this.setState({ playerTurnNotificationDialog: {visible: false, confirmed: true}});
  }

  closelastPlayerActionDialog() {
    this.setState({ lastPlayerActionDialog: {visible: false, confirmed: true}});
  }

  render() {
    if(this.state.leftGame) 
      return <Redirect to={{
        pathname: "/Conquerors/games-room", 
        state: { playerUsername: this.state.playerUsername }
      }} />;

    return (
      <div className="main">
        <div className="main-header">
          <span>Conquerors Game</span>
        </div>
        <div className="main-game-container">
          <div className="container-header">
            <div className="container-header-title">Game Title - {this.state.gameTitle}</div>
            <div className="container conquerors-container">
              <div className="game-details">
                {
                  this.state.gameData != null ? 
                  <span><b>Current Round: </b> 
                  {
                    !this.state.gameData.gameStarted || this.state.isGameOver ? 
                    "0 / " + this.state.gameData.totalRounds : 
                    (this.state.gameData.currentRound + 1) + " / " + this.state.gameData.totalRounds
                  }
                  </span> : null
                }
                {
                  this.state.currentPlayer != null ? 
                  <span><b>Current Player: </b> 
                  {this.state.currentPlayer.name}</span> 
                  : null
                }
              </div>
              <div className="game-controls">
                <button className="my-button" onClick={this.handleTakeAction} disabled={!this.state.isUsersTurn || !this.state.canTakeAction}>Take Action On Territory</button>
                <button className="my-button" onClick={this.handleEndTurn} disabled={!this.state.isUsersTurn}>End Turn</button>
                <button className="my-button" onClick={this.handleSurrender} disabled={!this.state.isUsersTurn}>Surrender</button>
              </div>
              {
                this.state.gameData != null ?
                <PlayersTable 
                  playersData={this.state.gameData.players}
                  initialFunds={this.state.gameData.initialFunds}
                  currentPlayer={this.state.currentPlayer}
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
                  playerUsername={this.state.playerUsername}
                  currentPlayer={this.state.currentPlayer}
                  openTerritoryArmiesDialog={this.openTerritoryArmiesDialog}
                  actionsOnTerritories={this.state.actionsOnTerritories}
                  takingActionOnTerritory={this.state.takingActionOnTerritory}
                /> : null
              }
              <Modal visible={this.state.territoryArmiesDialog.visible} effect="fadeInUp">
                  <TerritoryArmiesDialog 
                      gameTitle={this.state.gameTitle}
                      ref={this.territoryArmiesDialogRef}
                      currentPlayer={this.state.currentPlayer}
                      isUsersTurn={this.state.isUsersTurn}
                      openAddArmyDialogFunc={this.openAddArmyDialog}
                      closeFunction={this.closeTerritoryArmiesDialog}
                      unitsData={this.state.gameData ? this.state.gameData.gameUnits : null}
                      SERVLET_URL={SERVLET_URL}
                  />
              </Modal>
              <Modal visible={this.state.gameOverDialog.visible} effect="fadeInUp">
                  <GameOverDialog 
                      ref={this.gameOverDialogRef}
                      gameResults={this.state.gameResults}
                      leaveGameFunction={this.handleLeaveGame}
                      playerUsername={this.state.playerUsername}
                  />
              </Modal>
              <Modal visible={this.state.playerTurnNotificationDialog.visible && !this.state.playerTurnNotificationDialog.confirmed} effect="fadeInUp">
                  <PlayerTurnNotificationDialog 
                      closeFunction={this.closePlayerTurnNotificationDialog}
                  />
              </Modal>
              <Modal visible={this.state.lastPlayerAction != null && this.state.playerUsername != this.state.lastPlayerName &&
                              this.state.lastPlayerActionDialog.visible && !this.state.lastPlayerActionDialog.confirmed} effect="fadeInUp">
                  <LastPlayerActionDialog 
                    lastPlayerName={this.state.lastPlayerName}
                    lastPlayerAction={this.state.lastPlayerAction}
                    closeFunction={this.closelastPlayerActionDialog}
                  />
              </Modal>
              {
                this.state.gameData ?
                <Modal visible={this.state.gameData.totalPlayers != this.state.gameData.currentPlayers && !this.state.isGameOver} effect="fadeInUp">
                    <WaitingForPlayersDialog 
                        leaveGameFunction={this.handleLeaveGame}
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
