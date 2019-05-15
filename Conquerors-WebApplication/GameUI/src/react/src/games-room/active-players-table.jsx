import React, { Component } from 'react';

class ActivePlayersTable extends Component {

  constructor(){
    super();
  }

  // Populate the table with data based on the received data from the parent component
  createTable() {
    let table = []
    //let dataArray = this.props.data;
    let dataArray = [
      {
        id: 54,
        name: "Dude",
      },
    ];
    
    for (let i = 0; i < dataArray.length; i++) {
        let children = []
  
        children.push(<td key={dataArray[i].id+"-"+i+"id"}>{dataArray[i].id}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"name"}>{dataArray[i].name}</td>)

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
                <th>ID</th>
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
