import React, { Component } from 'react';
import ActiveGamesTable from './active-games-table.jsx';
import { Redirect } from "react-router-dom";
import ActivePlayersTable from './active-players-table.jsx';
import {postActionTo, getDataFrom} from '../server-functions.js';
import Modal from 'react-awesome-modal';
import ViewGameBoardDialog from './dialogs/view-game-board-dialog.jsx';
import ViewGameUnitsDialog from './dialogs/view-game-units-dialog.jsx';
import UploadGameResultsDialog from './dialogs/upload-game-results-dialog.jsx';
import './games-room.css';

const SERVLET_URL = "gamesRoom";

class GamesRoom extends Component {

  constructor(){
    super();
    
    this.state = {
      activeGames: [],
      activePlayers: [],
      loggedOut: false,
      joinedGame: false,
      joinedGameTitle: null,
      viewGameBoardDialog: {
          boardData: null,
          visible: false
      },
      viewGameUnitsDialog: {
        unitsData: null,
        visible: false
      },
      uploadGameResultDialog: {
        response: null,
        visible: false
      },
    }

    this.uploadFileInput = React.createRef();
    this.uploadFileForm = React.createRef();

    this.refreshDataInterval = null;
    this.refreshData = this.refreshData.bind(this);
    this.handleLogout = this.handleLogout.bind(this);
    this.handleUploadGameFile = this.handleUploadGameFile.bind(this);
    this.handleJoinGame = this.handleJoinGame.bind(this);
    this.formSubmit = this.formSubmit.bind(this);
    this.openViewGameBoardDialog = this.openViewGameBoardDialog.bind(this);
    this.closeViewGameBoardDialog = this.closeViewGameBoardDialog.bind(this);    
    this.openViewGameUnitsDialog = this.openViewGameUnitsDialog.bind(this);
    this.closeViewGameUnitsDialog = this.closeViewGameUnitsDialog.bind(this);  
    this.closeUploadGameResultDialog = this.closeUploadGameResultDialog.bind(this);  
  }

  componentDidMount(){
    this.refreshData();
    this.refreshDataInterval = setInterval(this.refreshData, 2000);
  }

  refreshData(){
    getDataFrom(SERVLET_URL, responseJSON => {
      this.setState({
        activeGames: responseJSON.gamesData,
        activePlayers: responseJSON.playersData
      });
    });
  }

  componentWillUnmount(){
    clearInterval(this.refreshDataInterval);
  }

  handleLogout(){
    postActionTo(SERVLET_URL, "logout", {}, () => this.setState({loggedOut: true}));
  }

  handleJoinGame(gameTitleParam){
    postActionTo(SERVLET_URL, "joinGame", {gameTitle: gameTitleParam}, () => this.setState({joinedGame: true, joinedGameTitle: gameTitleParam}));
  }

  formSubmit(){

    let formData = new FormData();
    formData.append("uploadedFile", this.uploadFileInput.current.files[0]);

    fetch('/Conquerors/uploadFile', { 
      method: 'POST',
      body: formData
    })
    .then(async response => {
      if(response){
        const responseJSON = await response.json();
        this.setState({uploadGameResultDialog: {
          response: responseJSON,
          visible: true
        }});
      }
    });
  }

  handleUploadGameFile(event){
    event.preventDefault();
    this.uploadFileInput.current.click();
  }

  openViewGameBoardDialog(boardDataParam) {
      this.setState({
          viewGameBoardDialog: {
              boardData: boardDataParam,
              visible: true,
          }
      });
  }

  closeViewGameBoardDialog() {
      this.setState({
          viewGameBoardDialog: {
              boardData: null,
              visible: false,
          }
      });
  }

  openViewGameUnitsDialog(unitsDataParam) {
    this.setState({
        viewGameUnitsDialog: {
            unitsData: unitsDataParam,
            visible: true,
        }
    });
  }

  closeViewGameUnitsDialog() {
      this.setState({
          viewGameUnitsDialog: {
              unitsData: null,
              visible: false,
          }
      });
  }

  closeUploadGameResultDialog() {
    this.setState({
        uploadGameResultDialog: {
            result: null,
            visible: false,
        }
    });
  }

  render() {
    if(this.state.loggedOut) 
      return <Redirect to="/Conquerors/" />;
    
    if(this.state.joinedGame) 
      return <Redirect to={{
                pathname: "/Conquerors/conquerors-game", 
                state: { gameTitle: this.state.joinedGameTitle }
              }}/>;

    return (
      <div className="main">
        <div className="main-header">
          Games Room
        </div>
        <form ref={this.uploadFileForm} method="POST" action="/Conquerors/uploadFile" hidden>
          <input ref={this.uploadFileInput} type="file" name="uploadedFile" onChange={this.formSubmit} hidden/>
        </form> 
        <div className="buttons-container">
          <button className="my-button" onClick={this.handleUploadGameFile}>Upload Game XML File</button>
          <button className="my-button" onClick={this.handleLogout}>Logout</button>
        </div>
        <div className="tables-container">
          <div className="container-header">
          <div className="container-header-title">Active Games</div>
            <div className="container">
              <ActiveGamesTable 
                gamesData={this.state.activeGames}
                openViewGameBoardDialog={this.openViewGameBoardDialog}
                openViewGameUnitsDialog={this.openViewGameUnitsDialog}
                handleJoinGame={this.handleJoinGame}
              />
            </div>
          </div>
          
          <div className="container-header">
            <div className="container-header-title">Active Players</div>
            <div className="container">
              <ActivePlayersTable playersData={this.state.activePlayers}/>
            </div>
          </div>
        </div>
        <Modal visible={this.state.viewGameBoardDialog.visible} effect="fadeInUp">
            <ViewGameBoardDialog 
                boardData={this.state.viewGameBoardDialog.boardData}
                closeFunction={this.closeViewGameBoardDialog}
            />
        </Modal>
        <Modal visible={this.state.viewGameUnitsDialog.visible} effect="fadeInUp">
            <ViewGameUnitsDialog 
                unitsData={this.state.viewGameUnitsDialog.unitsData}
                closeFunction={this.closeViewGameUnitsDialog}
            />
        </Modal>
        <Modal visible={this.state.uploadGameResultDialog.visible} effect="fadeInUp">
            <UploadGameResultsDialog 
                response={this.state.uploadGameResultDialog.response}
                closeFunction={this.closeUploadGameResultDialog}
            />
        </Modal>
      </div>
    );
  }
}

export default GamesRoom; 
