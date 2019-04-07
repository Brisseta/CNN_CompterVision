from django.db import models


# Create your models here.


class ClientSearch(models.Model):
    client_ip = models.GenericIPAddressField(blank=False)
    post_date = models.DateTimeField(auto_now=True)
    data_location = models.CharField(default="/img_searches/0", max_length=50)
    received = models.BooleanField(default=False)
    analyzed = models.BooleanField(default=False)

    class Meta:
        ordering = ('post_date',)

    def __str__(self):
        return "%s %s %d %s" % (
            self.client_ip, "date : ", self.post_date.day, self.data_location)


class Url(models.Model):
    result_lib = models.CharField(max_length=100, blank=False)
    count = models.IntegerField(default=0)
    score = models.FloatField(default=0)
    search = models.ForeignKey(ClientSearch, on_delete=models.DO_NOTHING, default=0, related_name='urls', blank=True)

    def __str__(self):
        return "%s %d %d" % ("libelle: " + self.result_lib, self.count, self.score)

    class Meta:
        get_latest_by = 'count'
