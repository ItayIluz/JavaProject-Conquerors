import React, { Component } from 'react';

class ActiveGamesTable extends Component {

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
        name: "Test",
        uploadedBy: "Dude",
        status: "pending",
        playersInGame: "0 / 4",
      }
    ];
    
    for (let i = 0; i < dataArray.length; i++) {
        let children = []
  
        children.push(<td key={dataArray[i].id+"-"+i+"id"} hidden>{dataArray[i].id}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"name"}>{dataArray[i].name}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"uploadedBy"}>{dataArray[i].uploadedBy}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"status"}>{dataArray[i].status}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"playersInGame"}>{dataArray[i].playersInGame}</td>)
        children.push(<td key={dataArray[i].id+"-"+i+"viewBoardButton"}><button className="my-button">View Board</button></td>)

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
              <th hidden>ID</th>
              <th>Name</th>
              <th>Uploaded By</th>
              <th>Status</th>
              <th>Players In-Game</th>
              <th></th>
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

export default ActiveGamesTable; 
