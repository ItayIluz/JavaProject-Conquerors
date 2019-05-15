import React, { Component } from 'react';

class PlayersTable extends Component {

  constructor(props){
    super(props);
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = []
    let dataArray = this.props.playersData;
    
    for (let i = 0; i < dataArray.length; i++) {
        let children = []
  
        children.push(<td key={dataArray[i].id+"-"+i+"name"}>{dataArray[i].name}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"id"}>{dataArray[i].id}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"color"} style={{background: dataArray[i].color}}>{dataArray[i].colorName}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"money"}>{this.props.initialFunds}</td>)

        table.push(<tr key={dataArray[i].id} data-id={dataArray[i].id}>{children}</tr>)
    }
    return table;
  }

  render() {
    return (
      <div className={"table-container" + (this.props.hidden ? " hidden" : "")}>
        <table className="my-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>ID</th>
              <th>Color</th>
              <th>Money</th>
            </tr>
          </thead>
          <tbody>
            {this.createTable()}
          </tbody>
        </table>
      </div>
    );
  }
}

export default PlayersTable; 
