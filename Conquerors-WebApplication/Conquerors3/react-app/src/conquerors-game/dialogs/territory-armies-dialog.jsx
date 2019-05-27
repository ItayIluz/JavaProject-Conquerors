import React, { Component } from 'react';

class TerritoryArmiesDialog extends Component {

  constructor(props){
    super(props);
    this.state ={
      showConfirm: true,
      canConfirm: false,
      showRemove: true,
      canRemove: false,
      showAdd: true,
      showFixButton: true,
      showFixDetail: true,
      canFix: true,
      armies: [
        {
          unit: "Soldier",
          amount: "1",
          firepower: "100",
          costToFix: "0",
        }
      ]
    }

    this.detailCostToFix = React.createRef();
    this.detailFirepower = React.createRef();
    this.detailNewArmiesCost = React.createRef();
  }

  componentDidMount(){

    switch(this.props.mode){
      case "View": {
        this.setState({showConfirm: false, showRemove: false, showAdd: false, showFixButton: false, showFixDetail: true})
        break;
      } case "Maintain": {
        this.setState({showConfirm: true, showRemove: true, showAdd: true, showFixButton: true, showFixDetail: true})
        break;
      } case "Conquer" || "Attack": {
        this.setState({showConfirm: true, showRemove: true, showAdd: true, showFixButton: false, showFixDetail: false})
        break;
      }
    }
  }

  createTable() {
    let table = [];
    let armiesData = this.state.armies;
    
    for (let i = 0; i < armiesData.length; i++) {
        let children = []
  
        children.push(<td key={armiesData[i].id+"-"+i+"unit"}>{armiesData[i].unit}</td>)
        children.push(<td key={armiesData[i].id+"-"+i+"amount"}>{armiesData[i].amount}</td>)
        children.push(<td key={armiesData[i].id+"-"+i+"firepower"}>{armiesData[i].firepower}</td>)
        children.push(<td key={armiesData[i].id+"-"+i+"costToFix"} style={{display: this.state.showFixDetail ? "block" : "none"}}>{armiesData[i].costToFix}</td>);

        table.push(<tr key={"army-"+armiesData[i].id} data-id={armiesData[i].id}>{children}</tr>)
    }
    return table;
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Territory Armies</div>
        <div className="container dialog-container">
          {
            this.state.armies.length != 0 ? 
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
            <div className="dialog-detail">Total New Armies Cost: <span ref={this.detailNewArmiesCost}></span></div>
            <div className="dialog-detail">Total Firepower: <span ref={this.detailFirepower}></span></div>
            <div className="dialog-detail" style={{display: this.state.showFixDetail ? "inline" : "none"}}>Total Cost To Fix: <span ref={this.detailCostToFix}></span></div>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction} disabled={!this.state.canConfirm} style={{display: this.state.showConfirm ? "inline" : "none"}}>Confirm</button>
            <button className="my-button dialog-button" onClick={() => this.props.openAddArmyDialogFunc(this.props.territory, this.props.currentPlayer)} style={{display: this.state.showAdd ? "inline" : "none"}}>Add...</button>
            <button className="my-button dialog-button" onClick={this.props.closeFunction} disabled={!this.state.canRemove} style={{display: this.state.showRemove ? "inline" : "none"}}>Remove</button>
            <button className="my-button dialog-button" onClick={this.props.closeFunction} style={{display: this.state.showFixButton ? "inline" : "none"}}>Fix Competence</button>
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default TerritoryArmiesDialog; 