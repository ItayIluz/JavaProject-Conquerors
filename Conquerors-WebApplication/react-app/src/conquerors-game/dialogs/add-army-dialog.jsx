import React, { Component } from 'react';
import "./add-army-dialog.css";

class AddArmyDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      canConfirm: false,
      playerHasEnoughMoney: true,
      createdArmy: null,
      totalFirepower: 0,
      totalCost: 0,
    }

    this.unitType = React.createRef();
    this.amount = React.createRef();

    this.handleInputChange = this.handleInputChange.bind(this);
    this.resetElements = this.resetElements.bind(this);
    this.onOpenDialog = this.onOpenDialog.bind(this);
    this.handleConfirm = this.handleConfirm.bind(this);
  }

  handleConfirm(){
    this.props.closeFunction({
      unit: {type: this.unitType.current.value},
      amount: this.amount.current.value,
      competence: this.state.totalFirepower,
      totalCost: this.state.totalCost,
      isNew: true,
    });
  }

  handleInputChange(){
    let selectedUnit = this.unitType.current.value;
    let amountToAdd = this.amount.current.value;
    let currentPlayer = this.props.currentPlayer;

    if(selectedUnit && amountToAdd){
        if (amountToAdd > 0) { // Must add more than 0
          
            let totalCost = parseInt(this.props.unitsData[selectedUnit].purchasePrice * amountToAdd);
            let totalFirepower = parseInt(this.props.unitsData[selectedUnit].maxFirePower * amountToAdd);

            if (currentPlayer.money < totalCost) { // If the player doesn't have enough money
                this.setState({playerHasEnoughMoney: false, canConfirm: false, totalCost: totalCost, totalFirepower: totalFirepower})
            } else {
                this.setState({
                  playerHasEnoughMoney: true, 
                  canConfirm: true,
                  totalCost: totalCost, 
                  totalFirepower: totalFirepower,
                  createdArmy: {
                    amountToAdd: amountToAdd,
                    selectedUnit: selectedUnit,
                    totalCost: totalCost,
                    totalFirepower: this.props.unitsData[selectedUnit].maxFirePower
                  }
                })
            }
        } else
          this.resetElements();
    } else
      this.resetElements();
  }

  onOpenDialog(){
    this.unitType.current.value = "";
    this.amount.current.value = "";
    this.resetElements();
  }

  resetElements(){
    this.setState({
      playerHasEnoughMoney: true, 
      canConfirm: false, 
      createdArmy: null,
      totalFirepower: 0,
      totalCost: 0,
    });
  }

  createUnitTypeOptions(){
    let options = [];
    let unitsData = this.props.unitsData;
    
    options.push(<option key={"unit-null"} value=""></option>);
    for(let unitType in this.props.unitsData){

      let text = unitType + 
                ", Cost " + unitsData[unitType].purchasePrice + 
                ", Rank " + unitsData[unitType].rank + 
                ", Firepower " + unitsData[unitType].maxFirePower;

      options.push(<option key={"unit-"+unitType} rank={unitsData[unitType].rank} value={unitType}>{text}</option>)
    }
    options.sort((a,b)=>a.rank-b.rank);
    return options;
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Add Army</div>
        <div className="container dialog-container add-army-dialog-container">
          <div>
            <div className="dialog-detail">Unit Type:</div>
          </div>
          <div>
           <select ref={this.unitType} onChange={this.handleInputChange}>
            {this.createUnitTypeOptions()}
           </select>
          </div>
          <div>
            <div className="dialog-detail">Amount:</div>
          </div>
          <div>
           <input type="number" ref={this.amount} onChange={this.handleInputChange}></input>
          </div>
          <div>
            <div className="dialog-detail">Total Cost:</div>
          </div>
          <div>
           <div><span>{this.state.totalCost} Turings</span></div>
           <div><span style={{color: "red", display: (this.state.playerHasEnoughMoney ? "none" : "inline")}}>Not enough money!</span></div>
          </div>
          <div>
            <div className="dialog-detail">Total Firepower:</div>
          </div>
          <div>
           <span>{this.state.totalFirepower} Turings</span>
          </div>
          <div>
            <button className="my-button dialog-button" onClick={this.handleConfirm} disabled={!this.state.canConfirm}>Confirm</button>
          </div>
          <div>
            <button className="my-button dialog-button" onClick={() => this.props.closeFunction(null)}>Cancel</button>
          </div>
        </div>
      </div>
    );
  }
}

export default AddArmyDialog; 