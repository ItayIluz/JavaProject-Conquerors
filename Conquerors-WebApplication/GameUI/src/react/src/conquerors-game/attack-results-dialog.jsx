import React, { Component } from 'react';
import './attack-results-dialog.css';

class AttackResultsDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      winningArmy: [
        {
          unit: "Soldier",
          amount: "1",
          firepower: "100",
        },
      ]
    }
  }

  createTable(armiesData) {
    let table = [];
    
    if(armiesData){
      for (let i = 0; i < armiesData.length; i++) {
          let children = []
    
          children.push(<td key={armiesData[i].unit+"-"+i+"unit"}>{armiesData[i].unit}</td>)
          children.push(<td key={armiesData[i].unit+"-"+i+"amount"}>{armiesData[i].amount}</td>)
          children.push(<td key={armiesData[i].unit+"-"+i+"firepower"}>{armiesData[i].firepower}</td>)

          table.push(<tr key={"army-"+armiesData[i].unit}>{children}</tr>)
      }
      return table;
    }
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Attack Results</div>
        <div className="container dialog-container attack-results-dialog-container">
          <div className="attack-results-details">
            Results
          </div>
          <div className="attack-results-attacking-army-table">
            <table className="my-table dialog-table">
            <thead>
              <tr><th colSpan="3">Attacking Army</th></tr>
              <tr>
                <th>Unit Type</th>
                <th>Amount</th>
                <th>Firepower</th>
              </tr>
            </thead>
            <tbody>
                {this.createTable(this.props.attackingArmy)}
            </tbody>
            </table>
          </div>
          <div className="attack-results-defending-army-table">
            <table className="my-table dialog-table">
            <thead>
              <tr><th colSpan="3">Defending Army</th></tr>
              <tr>
                <th>Unit Type</th>
                <th>Amount</th>
                <th>Firepower</th>
              </tr>
            </thead>
            <tbody>
                {this.createTable(this.props.defendingArmy)}
            </tbody>
            </table>
          </div>
          <div className="attack-results-winning-army-table">
            <table className="my-table dialog-table">
            <thead>
              <tr><th colSpan="3">Winning Army</th></tr>
              <tr>
                <th>Unit Type</th>
                <th>Amount</th>
                <th>Firepower</th>
              </tr>
            </thead>
            <tbody>
                {this.createTable(this.state.winningArmy)}
            </tbody>
            </table>
          </div>
          <div className="attack-results-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default AttackResultsDialog; 