import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import Modal from 'react-awesome-modal';
import { BrowserRouter, Route, Link } from "react-router-dom";
import "./index.css";

import TerritoryArmiesDialog from "./conquerors-game/territory-armies-dialog.jsx";
import AddArmyDialog from './conquerors-game/add-army-dialog.jsx';
import AttackResultsDialog from './conquerors-game/attack-results-dialog.jsx';
import GameOverDialog from './conquerors-game/game-over-dialog.jsx';
import SignUpPage from './sign-up-page/sign-up-page.jsx';
import GamesRoom from './games-room/games-room.jsx';
import ConquerorsGame from './conquerors-game/conquerors-game.jsx';

class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            territoryArmiesDialog: {
                visibile: false,
                territory: null,
                currentPlayer: null,
                mode: "Conquer"
            },
            addArmyDialog: {
                visibile: false,
                currentPlayer: null,
                territory: null,
            },
            attackResultsDialog: {
                visibile: false,
                attackType: "",
                territory: null,
                attackingArmy: null,
                attackingPlayer: null,
                defendingArmy: null,
                defendingPlayer: null
            },
            gameOverDialog: {
                visible: false
            }
        }

        this.openTerritoryArmiesDialog = this.openTerritoryArmiesDialog.bind(this);
        this.closeTerritoryArmiesDialog = this.closeTerritoryArmiesDialog.bind(this);
        this.openAddArmyDialog = this.openAddArmyDialog.bind(this);
        this.closeAddArmyDialog = this.closeAddArmyDialog.bind(this);
        this.openAttackResultsDialog = this.openAttackResultsDialog.bind(this);
        this.closeAttackResultsDialog = this.closeAttackResultsDialog.bind(this);
        this.openGameOverDialog = this.openGameOverDialog.bind(this);
        this.closeGameOverDialog = this.closeGameOverDialog.bind(this);
    }

    openTerritoryArmiesDialog(territoryData, currentPlayerData, modeData) {
        this.setState({
            territoryArmiesDialog: {
                visibile: true,
                territory: territoryData,
                currentPlayer: currentPlayerData,
                mode: modeData
            }
        });
    }
    
    closeTerritoryArmiesDialog() {
        this.setState({
            territoryArmiesDialog: {
                visibile: false,
                territory: null,
            }
        });
    }

    openAddArmyDialog(territoryData, currentPlayerData) {
        this.setState({
            addArmyDialog: {
                visibile: true,
                territory: territoryData,
                currentPlayer: currentPlayerData,
            }
        });
    }
    
    closeAddArmyDialog() {
        this.setState({
            addArmyDialog: {
                visibile: false,
                territory: null,
            }
        });
    }

    openAttackResultsDialog(territoryData, attackingArmyData, defendingArmyData, attackingPlayerData, defendingPlayerData, attackTypeData) {
        this.setState({
            attackResultsDialog: {
                visibile: true,
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
                visibile: false,
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
                visibile: true,
            }
        });
    }

    closeGameOverDialog() {
        this.setState({
            gameOverDialog: {
                visibile: false,
            }
        });
    }

    render(){
        return (
            <div>
                <ConquerorsGame openTerritoryArmiesDialog={this.openTerritoryArmiesDialog} /> 
                <Modal visible={this.state.territoryArmiesDialog.visibile} effect="fadeInUp">
                    <TerritoryArmiesDialog 
                        territory={this.state.territoryArmiesDialog.territory} 
                        currentPlayer={this.state.territoryArmiesDialog.currentPlayer} 
                        mode={this.state.territoryArmiesDialog.mode}
                        openAddArmyDialogFunc={this.openAddArmyDialog}
                        closeFunction={this.closeTerritoryArmiesDialog}
                    />
                </Modal>
                <Modal visible={this.state.addArmyDialog.visibile} effect="fadeInUp">
                    <AddArmyDialog 
                        territory={this.state.addArmyDialog.territory} 
                        currentPlayer={this.state.addArmyDialog.currentPlayer} 
                        mode={this.state.addArmyDialog.mode}
                        closeFunction={this.closeAddArmyDialog}
                    />
                </Modal>
                <Modal visible={this.state.attackResultsDialog.visibile} effect="fadeInUp">
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
                <Modal visible={this.state.gameOverDialog.visibile} effect="fadeInUp">
                    <GameOverDialog 
                        closeFunction={this.closeGameOverDialog}
                    />
                </Modal>
            </div>
        );
    }
}

      /*  <BrowserRouter>
            <SignUpPage /> 
            <Route exact path="/" component={SignUpPage} />
            <Route path="/about" component={GamesRoom} />
            <Route path="/topics" component={ConquerorsGame} />
        </BrowserRouter>      */
ReactDOM.render(<App />, document.getElementById("root"));
