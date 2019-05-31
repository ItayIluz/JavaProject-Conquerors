import React, { Component } from 'react';

class ActivePlayersTable extends Component {

  constructor(){
    super();
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = []
    let playersData = this.props.playersData;
    
    for (let i = 0; i < playersData.length; i++) {
        let children = []
  
        children.push(<td key={"player-cell-"+playersData[i]+"-"+i}>{playersData[i]}</td>)

        table.push(<tr key={"player-row-"+playersData[i]}>{children}</tr>)
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

export default ActivePlayersTable; 
