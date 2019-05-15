import React, { Component } from 'react';
import "./add-army-dialog.css";

class AddArmyDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      canConfirm: false,
      playerHasEnoughMoney: true,
      unitsData: {
        "Soldier": {
            purchasePrice: 80,
            rank: 1,
            firepower: 100
        }
      },
      createdArmy: null
    }

    this.unitType = React.createRef();
    this.amount = React.createRef();
    this.totalCost = React.createRef();
    this.totalFirepower = React.createRef();

    this.handleInputChange = this.handleInputChange.bind(this);
    this.resetElements = this.resetElements.bind(this);
  }

  handleInputChange(){
    let selectedUnit = this.unitType.current.value;
    let amountToAdd = this.amount.current.value;
    let currentPlayer = this.props.currentPlayer;

    if(selectedUnit && amountToAdd){
        if (amountToAdd > 0) { // Must add more than 0
          
            let totalCost = this.state.unitsData[selectedUnit].purchasePrice * amountToAdd;
            let totalFirepower = this.state.unitsData[selectedUnit].firepower * amountToAdd;
            this.totalCost.current.innerHTML = totalCost + " Turings";
            this.totalFirepower.current.innerHTML = totalFirepower;

            if (currentPlayer.money < totalCost) { // If the player doesn't have enough money
                this.setState({playerHasEnoughMoney: false, canConfirm: false})
            } else {
                this.setState({
                  playerHasEnoughMoney: true, 
                  canConfirm: true,
                  createdArmy: {
                    amountToAdd: amountToAdd,
                    selectedUnit: selectedUnit,
                    totalCost: totalCost,
                    totalFirepower: this.state.unitsData[selectedUnit].firepower
                  }
                })
            }
        } else
          this.resetElements();
    } else
      this.resetElements();
  }

  resetElements(){
    this.setState({playerHasEnoughMoney: true, canConfirm: false, createdArmy: null});
    this.totalCost.current.innerHTML = "0 Turings";
    this.totalFirepower.current.innerHTML = "0";
  }

  createUnitTypeOptions(){
    let options = [];
    let unitsData = this.state.unitsData;
    
    options.push(<option key={"unit-null"} value=""></option>);
    for(let unitType in this.state.unitsData){

      let text = unitType + 
                ", Cost " + unitsData[unitType].purchasePrice + 
                ", Rank " + unitsData[unitType].rank + 
                ", Firepower " + unitsData[unitType].firepower;

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
           <div><span ref={this.totalCost}>0 Turings</span></div>
           <div><span style={{color: "red", display: (this.state.playerHasEnoughMoney ? "none" : "inline")}}>Not enough money!</span></div>
          </div>
          <div>
            <div className="dialog-detail">Total Firepower:<span ref={this.detailNewArmiesCost}></span></div>
          </div>
          <div>
           <span ref={this.totalFirepower}>0</span>
          </div>
          <div>
            <button className="my-button dialog-button" onClick={this.props.closeFunction} disabled={!this.state.canConfirm}>Confirm</button>
          </div>
          <div>
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Cancel</button>
          </div>
        </div>
      </div>
    );
  }
}

export default AddArmyDialog; 