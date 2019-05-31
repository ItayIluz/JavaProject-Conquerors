import React, { Component } from 'react';
import './upload-game-results-dialog.css';

class UploadGameResultsDialog extends Component {

  constructor(props){
    super(props);

    this.createErrors = this.createErrors.bind(this);
  }

  createErrors(){
      let errorsList = []
      
      for (let i = 0; i < this.props.response.errors.length; i++) {
          errorsList.push(<li key={"error"+i}>{this.props.response.errors[i]}</li>)
      }
      return errorsList;
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Upload Results</div>
        <div className="container dialog-container">
          <div style={{margin: "0px 20px"}}>
            <u><b>
              {this.props.response && this.props.response.result == "SUCCESS" ? "Game file uploaded successfully!" : "The following errors occured during game file upload:"}
            </b></u>
              {this.props.response && this.props.response.result == "ERROR" ? <div className="errors-list"><ul>{this.createErrors()}</ul></div> : null}
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default UploadGameResultsDialog; 