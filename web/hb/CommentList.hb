<div id="CommentList" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title">Comments</h4>
            </div>
            <div class="modal-body">
                {{#each comment}}
                    <h1 class="CommentList-commentUser">this.user</h1>
                    <h1 class="CommentList-commentText">this.text</h1>
                {{/each}}
                <textarea class="form-control" id="CommentList-ownComment"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" id="CommentList-OK">OK</button>
                <button type="button" class="btn btn-default" id="CommentList-Close">Close</button>
            </div>
        </div>
    </div>
</div>