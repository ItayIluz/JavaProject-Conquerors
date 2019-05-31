import React, { Component } from 'react';
import './view-game-board-dialog.css';

class ViewGameUnitsDialog extends Component {

  constructor(props){
    super(props);
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = []
    let unitsData = [];

    for(let unit in this.props.unitsData)
      unitsData.push(this.props.unitsData[unit]);
    
    unitsData.sort((a,b) => a.rank - b.rank);

   for (let i = 0; i < unitsData.length; i++) {
        let children = []
        children.push(<td key={unitsData[i].type+"-"+"type"}>{unitsData[i].type}</td>)
        children.push(<td key={unitsData[i].type+"-"+"rank"}>{unitsData[i].rank}</td>)
        children.push(<td key={unitsData[i].type+"-"+"cost"}>{unitsData[i].purchasePrice}</td>)
        children.push(<td key={unitsData[i].type+"-"+"maxFirePower"}>{unitsData[i].maxFirePower}</td>)
        children.push(<td key={unitsData[i].type+"-"+"competenceReduction"}>{unitsData[i].competenceReduction}</td>)
        children.push(<td key={unitsData[i].type+"-"+"singleFirePowerPrice"}>{unitsData[i].singleFirePowerPrice.toFixed(2)}</td>)

        table.push(<tr key={"unit-row-"+unitsData[i].type} >{children}</tr>)
    }
    return table;
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Board Preview</div>
        <div className="container dialog-container">
          <div className={"table-container" + (this.props.hidden ? " hidden" : "")}>
          {
            this.props.unitsData && this.props.unitsData.length != 0 ? 
            <div>
              <table className="my-table">
                <thead>
                  <tr>
                    <th>Unit Type</th>
                    <th>Rank</th>
                    <th>Cost</th>
                    <th>Max Firepower</th>
                    <th>Competence Reduction</th>
                    <th>Single Firepower Cost</th>
                  </tr>
                </thead>
                <tbody>
                  {this.createTable()}
                </tbody>
              </table>
            </div> : null
          }
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default ViewGameUnitsDialog; 