<div id="UserPage" class=" modal fade" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">{{this.user}}</h4>
            </div>
            <div class="modal-body">
                <label for="UserPage-title">Title</label>
                <input class="form-control" type="text" id="UserPage-email" value="{{this.email}}" />
                <label for="UserPage-message">Message</label>
                <textarea class="form-control" id="UserPage-bio">{{this.bio}}</textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" id="UserPage-OK" data-value="{{this.id}}">OK</button>
                <button type="button" class="btn btn-default" id="UserPage-Close" data-value="{{this.id}}">Close</button>
            </div>
        </div>
    </div>
</div>