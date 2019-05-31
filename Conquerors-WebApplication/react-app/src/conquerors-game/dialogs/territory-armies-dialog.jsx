import React, { Component } from 'react';
import Modal from 'react-awesome-modal';
import AddArmyDialog from './add-army-dialog.jsx';
import TerritoryArmiesErrorDialog from './territory-armies-error-dialog.jsx';
import { postActionTo } from '../../server-functions.js';
import ChooseAttackTypeDialog from './choose-attack-type-dialog.jsx';
import AttackResultsDialog from './attack-results-dialog.jsx';

class TerritoryArmiesDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      territory: null,
      mode: "",
      selectedArmyToRemove: null,
      showConfirm: true,
      canConfirm: false,
      showRemove: true,
      canRemove: false,
      showAdd: true,
      showFixButton: true,
      showFixDetail: true,
      canFix: true,
      totalCostToFix: 0,
      totalArmiesFirepower: 0,
      totalNewArmiesCost: 0,
      newArmies: [],
      addArmyDialog: {
        visible: false,
        territory: null,
      },
      territoryArmiesErrorDialog: {
        visible: false,
        errorMessage: null
      },
      attackResultsDialog: {
        visible: false,
        attackType: "",
        territory: null,
        showChooseAttackDialog: false,
        attackingArmy: null,
        attackingPlayerName: null,
        defendingPlayerName: null,
        defendingArmy: null,
        winningPlayerName: null,
        winningArmy: null,
        attackResultsDescription: null,
      },    
    }

    this.addArmyDialogRef = React.createRef();

    this.openAddArmyDialog = this.openAddArmyDialog.bind(this);
    this.closeAddArmyDialog = this.closeAddArmyDialog.bind(this);
    this.closeTerritoryArmiesErrorDialog = this.closeTerritoryArmiesErrorDialog.bind(this);
    this.setMode = this.setMode.bind(this);
    this.handleConfirm = this.handleConfirm.bind(this);
    this.handleFix = this.handleFix.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
    this.onOpenDialog = this.onOpenDialog.bind(this);
    this.onCloseDialog = this.onCloseDialog.bind(this);
    this.openAttackResultsDialog = this.openAttackResultsDialog.bind(this);
    this.closeAttackResultsDialog = this.closeAttackResultsDialog.bind(this);
    this.openChooseAttackTypeDialog = this.openChooseAttackTypeDialog.bind(this);
    this.closeChooseAttackTypeDialog = this.closeChooseAttackTypeDialog.bind(this);
    this.selectArmyToRemove = this.selectArmyToRemove.bind(this);
  }

  onOpenDialog(territoryData, modeData){
    let totalCostToFix = 0, totalFirepower = 0;
    if(modeData !== "Attack"){
      let armiesData = territoryData.armies.concat(this.state.newArmies);
      
      for(let i = 0; i < armiesData.length; i++){
        totalCostToFix += Math.round(armiesData[i].unit.singleFirePowerPrice * ((armiesData[i].unit.maxFirePower * armiesData[i].amount) - armiesData[i].competence)) || 0;
        totalFirepower += armiesData[i].competence;
      }
    }

    this.setMode(modeData);
    this.setState(() => {
      return {
        canConfirm: false,
        canRemove: false,
        canFix: true,
        totalCostToFix: totalCostToFix,
        totalArmiesFirepower: totalFirepower,
        totalNewArmiesCost: 0,
        territory: territoryData,
        mode: modeData,
        newArmies: []
      };
    });
  }

  setMode(mode){
    switch(mode){
      case "View": {
        this.setState({showConfirm: false, showRemove: false, showAdd: false, showFixButton: false, showFixDetail: true});
        break;
      } case "Maintain": {
        this.setState({showConfirm: true, showRemove: true, showAdd: true, showFixButton: true, showFixDetail: true});
        break;
      } default: { //case "Conquer" || "Attack": 
        this.setState({showConfirm: true, showRemove: true, showAdd: true, showFixButton: false, showFixDetail: false});
      }
    }
  }

  createTable() {
    let table = [];
    let armiesData;
    if(this.state.mode !== "Attack")
      armiesData = this.state.newArmies.concat(this.state.territory.armies);
    else
      armiesData = this.state.newArmies;

    for (let i = 0; i < armiesData.length; i++) {
        let children = [];

        let costToFix = Math.round(armiesData[i].unit.singleFirePowerPrice * ((armiesData[i].unit.maxFirePower * armiesData[i].amount) - armiesData[i].competence)) || 0;

        children.push(<td key={armiesData[i].unit.type+"-"+i+"unitType"}>{armiesData[i].unit.type}</td>);
        children.push(<td key={armiesData[i].unit.type+"-"+i+"amount"}>{armiesData[i].amount}</td>);
        children.push(<td key={armiesData[i].unit.type+"-"+i+"competence"}>{armiesData[i].competence}</td>);
        children.push(<td key={armiesData[i].unit.type+"-"+i+"costToFix"} style={{display: this.state.showFixDetail ? "block" : "none"}}>{costToFix}</td>);

        table.push(<tr key={"army-"+i+"-"+armiesData[i].unit.type} onClick={(event) => this.selectArmyToRemove(armiesData[i], event)}>{children}</tr>)
    }

    return table;
  }

  selectArmyToRemove(armyToRemove, event){
    if(armyToRemove.isNew === true) {
      let row = event.target.parentNode;
      let table = row.parentNode;

      if(row.style.background == "lightgray"){
        row.style.background = "none";
        this.setState({selectedArmyToRemove: null, canRemove: false});
      } else {
        for(let row of table.children)
          row.style.background = "none";

        event.target.parentNode.style.background = "lightgray";
        this.setState({selectedArmyToRemove: armyToRemove, canRemove: true});
      }
    }
  }

  openAddArmyDialog() {
      this.addArmyDialogRef.current.onOpenDialog();
      this.setState({
        addArmyDialog: {
            visible: true,
            territory: this.state.territory,
        }
      });
  }

  closeAddArmyDialog(newArmy) {

      let newNewArmies = this.state.newArmies;

      if(newArmy != null)
        newNewArmies.push(newArmy);

      this.setState({
        canConfirm: newNewArmies.length > 0,
        newArmies: newNewArmies,
        totalArmiesFirepower: newArmy != null ? this.state.totalArmiesFirepower + newArmy.competence : this.state.totalArmiesFirepower,
        totalNewArmiesCost: newArmy != null ? this.state.totalNewArmiesCost + newArmy.totalCost : this.state.totalNewArmiesCost,
        addArmyDialog: {
            visible: false,
            territory: null,
        }
      });
  }

  closeTerritoryArmiesErrorDialog(){
    this.setState({
      territoryArmiesErrorDialog: {
        visible: false,
        errorMessage: null
      }
    })
  }

  handleConfirm(){
    if(this.state.territory.armyThreshold > this.state.totalArmiesFirepower){
      this.setState({
        territoryArmiesErrorDialog: {
          visible: true,
          errorMessage: "You must add units with at least " + this.state.territory.armyThreshold + " firepower to conquer this territory!"
      }});
    } else if(this.props.currentPlayer.money < this.state.totalNewArmiesCost){
      this.setState({
        territoryArmiesErrorDialog: {
          visible: true,
          errorMessage: "You don't have enough Turings!"
      }});
    } else {
      if(this.state.mode == "Attack"){
        this.openChooseAttackTypeDialog();
      } else { /// Conquer or Maintain
        postActionTo(this.props.SERVLET_URL, "addArmiesToTerritory", {
          gameTitle: this.props.gameTitle,
          armies: this.state.newArmies,
          territoryId: this.state.territory.id,
          totalNewArmiesCost: this.state.totalNewArmiesCost
        }, () => this.onCloseDialog(true));
      }
    }
  }

  handleFix(){
    if(this.props.currentPlayer.money < this.state.totalCostToFix) {
      this.setState({
        territoryArmiesErrorDialog: {
          visible: true,
          errorMessage: "You don't have enough Turings!"
      }});
    } else {
      postActionTo(this.props.SERVLET_URL, "fixArmiesCompetence", {
        gameTitle: this.props.gameTitle,
        territoryId: this.state.territory.id,
        totalCostToFix: this.state.totalCostToFix
      }, () => this.onCloseDialog(true));
    }
  }
  
  handleRemove(){
    let currentNewArmies = this.state.newArmies;
    if(this.state.selectedArmyToRemove != null){
      let index = currentNewArmies.findIndex(a => a === this.state.selectedArmyToRemove);

      if (index > -1) {
        let removedArmy = currentNewArmies.splice(index, 1)[0];
        this.setState({
          canRemove: false,
          newArmies: currentNewArmies,
          totalArmiesFirepower: this.state.totalArmiesFirepower - removedArmy.competence,
          totalNewArmiesCost: this.state.totalNewArmiesCost - removedArmy.totalCost,
        });
      }
    }
  }

  onCloseDialog(tookAction){
    this.setState({newArmies: [], territory: null}); 
    this.props.closeFunction(tookAction);
  }

  openChooseAttackTypeDialog(){
    this.setState({ attackResultsDialog: { showChooseAttackDialog: true }});
  }

  closeChooseAttackTypeDialog(attackType){
    if(attackType == "Cancel")
      this.setState({attackResultsDialog: { showChooseAttackDialog: false }});
    else
      this.openAttackResultsDialog(attackType);
  }
  
  openAttackResultsDialog(attackTypeData) {
    let attackingPlayerName = this.props.currentPlayer.name;
    let defendingPlayerName = this.state.territory.conqueringPlayer.name;

    postActionTo(this.props.SERVLET_URL, "calculateAttackResults", {
      gameTitle: this.props.gameTitle,
      territoryId: this.state.territory.id,
      attackType: attackTypeData,
      armies: this.state.newArmies,
    }, responseJSON => {
      this.setState({
          attackResultsDialog: {
            showChooseAttackDialog: false,
            visible: true,
            territory: this.state.territory,
            attackingArmy: responseJSON.attackingArmy,
            attackingPlayerName: attackingPlayerName,
            defendingPlayerName: defendingPlayerName,
            defendingArmy: responseJSON.defendingArmy,
            winningPlayerName: responseJSON.winningPlayerName,
            winningArmy: responseJSON.winningArmy,
            attackResultsDescription: responseJSON.attackResultsDescription,
          }
      });
    });
  }

  closeAttackResultsDialog() {
    this.setState({
        attackResultsDialog: {
          visible: false,
          territory: null,
          attackingArmy: null,
          attackingPlayerName: null,
          defendingPlayerName: null,
          defendingArmy: null,
          winningPlayerName: null,
          winningArmy: null,
          attackResultsDescription: null,
        }
    }, () => this.onCloseDialog(true));
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Territory Armies</div>
        <div className="container dialog-container">
          {
            this.state.territory != null && ((this.state.territory.armies.length != 0 && this.state.mode !== "Attack") || this.state.newArmies.length != 0) ? 
            <div>
              <table className="my-table dialog-table">
                <thead>
                  <tr>
                    <th>Unit Type</th>
                    <th>Amount</th>
                    <th>Firepower</th>
                    <th style={{display: this.state.showFixDetail ? "block" : "none"}}>Cost To Fix</th>
                  </tr>
                </thead>
                <tbody>
                  {this.createTable()}
                </tbody>
              </table>
            </div> : 
            <div style={{margin: "20px"}}>No Armies.</div>
          }
          <div className="dialog-details-panel">
            { this.state.showAdd ? <div className="dialog-detail">Total New Armies Cost: <span>{this.state.totalNewArmiesCost}</span></div> : null }
            <div className="dialog-detail">Total Firepower: <span>{this.state.totalArmiesFirepower}</span></div>
            { this.state.showFixDetail ? <div className="dialog-detail">Total Cost To Fix: <span>{this.state.totalCostToFix}</span></div> : null }
          </div>
          <div className="dialog-button-panel">
            { this.state.showConfirm ? <button className="my-button dialog-button" onClick={this.handleConfirm} disabled={!this.state.canConfirm}>Confirm</button> : null}
            { this.state.showAdd ? <button className="my-button dialog-button" onClick={this.openAddArmyDialog}>Add...</button> : null }
            { this.state.showRemove ? <button className="my-button dialog-button" onClick={this.handleRemove} disabled={!this.state.canRemove}>Remove</button> : null }
            { this.state.showFixButton ? <button className="my-button dialog-button" onClick={this.handleFix}>Fix Competence</button> : null }
            <button className="my-button dialog-button" onClick={() => this.onCloseDialog(false)}>Close</button>
          </div>
        </div>
        <Modal visible={this.state.addArmyDialog.visible} effect="fadeInUp">
            <AddArmyDialog 
                ref={this.addArmyDialogRef}
                territory={this.state.addArmyDialog.territory} 
                currentPlayer={this.props.currentPlayer}
                mode={this.state.addArmyDialog.mode}
                closeFunction={this.closeAddArmyDialog}
                newArmies={this.state.newArmies}
                unitsData={this.props.unitsData}
            />
        </Modal>
        <Modal visible={this.state.territoryArmiesErrorDialog.visible} effect="fadeInUp">
            <TerritoryArmiesErrorDialog 
                errorMessage={this.state.territoryArmiesErrorDialog.errorMessage}
                closeFunction={this.closeTerritoryArmiesErrorDialog}
            />
        </Modal>
        <Modal visible={this.state.attackResultsDialog.visible} effect="fadeInUp">
            <AttackResultsDialog 
                territory={this.state.attackResultsDialog.territory}
                closeFunction={this.closeAttackResultsDialog}
                attackingArmy={this.state.attackResultsDialog.attackingArmy}
                attackingPlayerName={this.state.attackResultsDialog.attackingPlayerName}
                defendingPlayerName={this.state.attackResultsDialog.defendingPlayerName}
                defendingArmy={this.state.attackResultsDialog.defendingArmy}
                winningPlayerName={this.state.attackResultsDialog.winningPlayerName}
                winningArmy={this.state.attackResultsDialog.winningArmy}
                attackResultsDescription={this.state.attackResultsDialog.attackResultsDescription}
            />
        </Modal>
        <Modal visible={this.state.attackResultsDialog.showChooseAttackDialog} effect="fadeInUp">
            <ChooseAttackTypeDialog 
              closeFunction={this.closeChooseAttackTypeDialog}
            />
        </Modal>
      </div>
    );
  }
}

export default TerritoryArmiesDialog; 