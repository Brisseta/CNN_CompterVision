class ResultFormat:
    def __init__(self, score, url):
        self.score = score
        self.url = url

    def __str__(self):
        return "score %f , url : %s" % (self.score, self.url)
